#!/usr/bin/env python3
"""
One-shot generator for the Jetpack Compose rewrite of FactSheet.

Reads the legacy Java activities + HTML assets and emits a pair of data files
consumed at runtime by the new single-Activity Compose app:

  app/src/main/assets/gen/screens.json   - navigation graph (all screens)
  app/src/main/assets/gen/content.json   - native content blocks per html key

It also prints the set of images actually referenced so we can delete the rest.
"""
import re, os, json, glob, sys
from bs4 import BeautifulSoup, NavigableString, Tag

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JDIR = os.path.join(ROOT, "app/src/main/java/com/HIVFactsheet")
ADIR = os.path.join(ROOT, "app/src/main/assets")
OUT  = os.path.join(ADIR, "gen")
os.makedirs(OUT, exist_ok=True)

# ----------------------------------------------------------------------------
# 1. Parse every Java activity: fully-qualified class -> title, html, targets
# ----------------------------------------------------------------------------
PKG = "com.HIVFactsheet"

def fqn_for_file(path):
    rel = os.path.relpath(path, JDIR)[:-5]           # strip .java
    parts = rel.split(os.sep)
    return PKG + "." + ".".join(parts)

java_files = sorted(glob.glob(JDIR + "/**/*.java", recursive=True))

classes = {}    # fqn -> dict(simple, pkg, title, html, targets[simple], imports{simple:fqn})
simple_index = {}  # simple name -> list of fqns

for jf in java_files:
    src = open(jf, encoding="utf-8", errors="ignore").read()
    fqn = fqn_for_file(jf)
    simple = fqn.rsplit(".", 1)[1]
    pkg = fqn.rsplit(".", 1)[0]

    title_m = re.search(r'setText\(\s*"([^"]*)"\s*\)', src)
    title = title_m.group(1).strip() if title_m else ""

    html_m = re.search(r'file:///android_asset/([^"\'\s]+)', src)
    html = html_m.group(1) if html_m else ""

    # imports of other app classes
    imports = {}
    for im in re.findall(r'import\s+(' + re.escape(PKG) + r'\.[A-Za-z0-9_.]+)\s*;', src):
        imports[im.rsplit(".", 1)[1]] = im

    # startActivity(new Intent(..., X.class)) targets
    targets = []
    for t in re.findall(r'new\s+Intent\s*\([^,]*,\s*([A-Za-z0-9_]+)\.class', src):
        if t != simple and t not in targets:
            targets.append(t)

    classes[fqn] = dict(simple=simple, pkg=pkg, title=title, html=html,
                        targets=targets, imports=imports, src_url=html_m)
    simple_index.setdefault(simple, []).append(fqn)

def resolve(target_simple, owner_fqn):
    """Resolve a target class simple-name to a fqn using the owner's imports."""
    owner = classes[owner_fqn]
    if target_simple in owner["imports"]:
        cand = owner["imports"][target_simple]
        if cand in classes:
            return cand
    # same package
    same = owner["pkg"] + "." + target_simple
    if same in classes:
        return same
    # unique global
    cands = simple_index.get(target_simple, [])
    if len(cands) == 1:
        return cands[0]
    # top-level preference
    top = PKG + "." + target_simple
    if top in classes:
        return top
    return cands[0] if cands else None

def screen_id(fqn):
    """Stable, route-safe id. Sub-packages are prefixed to avoid clashes."""
    rel = fqn[len(PKG) + 1:]           # e.g. RH.queer or overview
    return rel.replace(".", "_")

# ----------------------------------------------------------------------------
# 2. HTML -> native content blocks
# ----------------------------------------------------------------------------
REFERS = os.path.join(ADIR, "refers")
used_images = set()

def content_key(html_rel):
    """refers/about/sa.html -> about_sa ; refers/whatishiv.html -> whatishiv"""
    rel = re.sub(r'^refers/', '', html_rel)
    rel = re.sub(r'\.(html?|php)$', '', rel)
    rel = re.sub(r'\.php$', '', rel)
    return re.sub(r'[^A-Za-z0-9]+', '_', rel).strip('_').lower()

INLINE_SKIP = {"script", "style", "head", "noscript"}

def collect_spans(el):
    """Flatten inline content of a block element into styled text runs."""
    spans = []
    def walk(node, bold, ital, color):
        if isinstance(node, NavigableString):
            txt = re.sub(r'\s+', ' ', str(node))
            if txt:
                spans.append({"text": txt, "b": bold, "i": ital, "c": color})
            return
        if not isinstance(node, Tag) or node.name in INLINE_SKIP:
            return
        if node.name == "br":
            spans.append({"text": "\n", "b": bold, "i": ital, "c": color})
            return
        b = bold or node.name in ("b", "strong")
        it = ital or node.name in ("i", "em")
        c = color
        style = node.get("style", "")
        m = re.search(r'color\s*:\s*([#\w]+)', style or "")
        if m:
            c = m.group(1)
        if node.get("color"):
            c = node.get("color")
        for ch in node.children:
            walk(ch, b, it, c)
    walk(el, False, False, None)
    # merge adjacent runs with identical styling
    merged = []
    for s in spans:
        if merged and merged[-1]["b"] == s["b"] and merged[-1]["i"] == s["i"] and merged[-1]["c"] == s["c"]:
            merged[-1]["text"] += s["text"]
        else:
            merged.append(dict(s))
    # drop null styling keys to keep json small
    out = []
    for s in merged:
        t = s["text"]
        if t.strip() == "" and t != "\n":
            # keep single spaces that join words, drop pure-noise
            if t == " " and out:
                out[-1]["text"] = out[-1]["text"].rstrip() + " "
            continue
        d = {"text": t}
        if s["b"]: d["b"] = 1
        if s["i"]: d["i"] = 1
        if s["c"]:
            col = normalize_color(s["c"])
            if col: d["c"] = col
        out.append(d)
    return out

def normalize_color(c):
    c = c.strip().lower()
    names = {"blue": "#1a4fd6", "red": "#e20719", "green": "#1d7a10",
             "white": None, "black": None}
    if c in names:
        return names[c]
    if re.match(r'^#[0-9a-f]{3,8}$', c):
        if len(c) == 4:  # #abc -> #aabbcc
            c = "#" + "".join(ch * 2 for ch in c[1:])
        if len(c) == 9:  # #aarrggbb -> keep rgb
            c = "#" + c[3:]
        if c.lower() in ("#ffffff", "#000000"):
            return None
        return c
    return None

def resolve_img(src, html_rel):
    if not src:
        return None
    src = src.strip()
    # absolute asset url: file:///android_asset/refers/images/x.png
    m = re.match(r'file:///android_asset/(.+)$', src)
    if m:
        cand = os.path.normpath(m.group(1))
    elif src.startswith(("http://", "https://", "data:", "#")):
        return None
    else:
        base = os.path.dirname("refers/" + re.sub(r'^refers/', '', html_rel))
        cand = os.path.normpath(os.path.join(base, src))
    full = os.path.join(ADIR, cand)
    if os.path.exists(full):
        used_images.add(cand)
        return cand
    # images are compressed to .webp after first generation; accept the sibling
    stem = os.path.splitext(cand)[0]
    webp = stem + ".webp"
    if os.path.exists(os.path.join(ADIR, webp)):
        used_images.add(webp)
        return webp
    return None

def spans_text(spans):
    return "".join(s["text"] for s in spans).strip()

def parse_html(html_rel):
    # resolve actual file (handle .php stubs that may not exist)
    path = os.path.join(ADIR, html_rel)
    if not os.path.exists(path):
        return None
    soup = BeautifulSoup(open(path, encoding="utf-8", errors="ignore").read(), "lxml")
    body = soup.body or soup
    for bad in body.find_all(INLINE_SKIP):
        bad.decompose()
    blocks = []

    def emit_para_or_heading(el):
        name = el.name
        spans = collect_spans(el)
        # pull images out of the paragraph
        imgs = [resolve_img(i.get("src"), html_rel) for i in el.find_all("img")]
        if spans_text(spans):
            if name in ("h1", "h2", "h3", "h4", "h5", "h6"):
                blocks.append({"t": "h", "lvl": min(3, int(name[1])), "s": spans})
            else:
                blocks.append({"t": "p", "s": spans})
        for im in imgs:
            if im:
                blocks.append({"t": "img", "src": im})

    def walk(node):
        for child in node.children:
            if not isinstance(child, Tag):
                continue
            n = child.name
            if n in INLINE_SKIP:
                continue
            if n in ("p", "h1", "h2", "h3", "h4", "h5", "h6"):
                emit_para_or_heading(child)
            elif n in ("ul", "ol"):
                ordered = 1 if n == "ol" else 0
                for li in child.find_all("li", recursive=False):
                    spans = collect_spans(li)
                    if spans_text(spans):
                        b = {"t": "li", "s": spans}
                        if ordered:
                            b["o"] = 1
                        blocks.append(b)
            elif n == "img":
                im = resolve_img(child.get("src"), html_rel)
                if im:
                    blocks.append({"t": "img", "src": im})
            elif n == "table":
                for tr in child.find_all("tr"):
                    cells = [spans_text(collect_spans(td)) for td in tr.find_all(["td", "th"])]
                    cells = [c for c in cells if c]
                    if cells:
                        blocks.append({"t": "p", "s": [{"text": "  •  ".join(cells)}]})
            elif n in ("br", "hr", "img"):
                continue
            else:
                walk(child)

    walk(body)
    # trim leading/trailing empty
    return blocks

# ----------------------------------------------------------------------------
# 3. Build registry + content
# ----------------------------------------------------------------------------
# Screens we render specially (external web content) rather than native blocks.
EXTERNAL = {
    PKG + ".videos":        "https://viralload.nascop.org/",
    PKG + ".Main39Activity":"https://www.youtube.com/channel/UCtU3EAv0jqgoEc65ZQxGutQ/videos",
}
# Screens that are never reachable UI (helper views / base widgets).
SKIP = {PKG + ".ScrollingButton", PKG + ".ScrollingTextview", PKG + ".ZoomLayout",
        PKG + ".Item", PKG + ".SplashActivity", PKG + ".homepage",
        PKG + ".utils.LoadingUtil", PKG + ".utils.MoveUpwardBehavior",
        PKG + ".MainActivity", PKG + ".PatientLitiracy"}

content = {}
screens = {}

for fqn, c in classes.items():
    if fqn in SKIP:
        continue
    sid = screen_id(fqn)
    entry = {"id": sid, "title": c["title"]}
    if fqn in EXTERNAL:
        entry["url"] = EXTERNAL[fqn]
    else:
        if c["html"]:
            key = content_key(c["html"])
            blocks = parse_html(c["html"])
            if blocks:
                content[key] = blocks
                entry["content"] = key
        kids = []
        for t in c["targets"]:
            r = resolve(t, fqn)
            if r and r not in SKIP and r in classes:
                kids.append(screen_id(r))
        # de-dupe, keep order
        seen = set(); kids = [k for k in kids if not (k in seen or seen.add(k))]
        if kids:
            entry["children"] = kids
    screens[sid] = entry

# The 4 main-menu tabs (ViewPager in legacy MainActivity).
tabs = [
    {"id": "you",     "title": "Facts About HIV"},
    {"id": "you10",   "title": "Reproductive Health"},
    {"id": "you6",    "title": "Find a Health Center"},
    {"id": "partners","title": "Youth Initiatives"},
]
registry = {
    "start": "you",          # first tab shown on the home menu
    "tabs": tabs,
    "screens": screens,
    "vlUrl": "https://viralload.nascop.org/",
    "youtubeUrl": EXTERNAL[PKG + ".Main39Activity"],
}

json.dump(registry, open(os.path.join(OUT, "screens.json"), "w"),
          ensure_ascii=False, separators=(",", ":"))
json.dump(content, open(os.path.join(OUT, "content.json"), "w"),
          ensure_ascii=False, separators=(",", ":"))

# ----------------------------------------------------------------------------
# 4. Report
# ----------------------------------------------------------------------------
print(f"screens: {len(screens)}   content keys: {len(content)}   used images: {len(used_images)}")
print(f"screens.json: {os.path.getsize(os.path.join(OUT,'screens.json'))//1024} KB")
print(f"content.json: {os.path.getsize(os.path.join(OUT,'content.json'))//1024} KB")
# save used-image list for the cleanup step
open(os.path.join(OUT, "used_images.txt"), "w").write("\n".join(sorted(used_images)))
# sanity: screens with neither content nor children nor url
empty = [s["id"] for s in screens.values()
         if "content" not in s and "children" not in s and "url" not in s]
print(f"empty screens ({len(empty)}): {sorted(empty)[:20]}")
# sanity: dangling child references
ids = set(screens) | {t["id"] for t in tabs}
dangling = sorted({k for s in screens.values() for k in s.get("children", []) if k not in ids})
print(f"dangling children: {dangling}")
