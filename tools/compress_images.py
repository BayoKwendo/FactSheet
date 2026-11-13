#!/usr/bin/env python3
"""Downscale + WebP-compress the content images and patch content.json srcs."""
import os, json, glob
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IMG = os.path.join(ROOT, "app/src/main/assets/refers/images")
CONTENT = os.path.join(ROOT, "app/src/main/assets/gen/content.json")
MAX_W = 1080

remap = {}          # old asset path -> new asset path
before = after = 0

for path in sorted(glob.glob(os.path.join(IMG, "*"))):
    if os.path.isdir(path):
        continue
    name = os.path.basename(path)
    before += os.path.getsize(path)
    try:
        im = Image.open(path)
    except Exception as e:
        print("skip", name, e)
        after += os.path.getsize(path)
        continue
    im = im.convert("RGBA" if im.mode in ("RGBA", "LA", "P") else "RGB")
    if im.width > MAX_W:
        h = round(im.height * MAX_W / im.width)
        im = im.resize((MAX_W, h), Image.LANCZOS)
    stem = os.path.splitext(name)[0]
    out = os.path.join(IMG, stem + ".webp")
    im.save(out, "WEBP", quality=80, method=6)
    after += os.path.getsize(out)
    if out != path:
        os.remove(path)
    remap["refers/images/" + name] = "refers/images/" + stem + ".webp"

# patch content.json image srcs
data = json.load(open(CONTENT, encoding="utf-8"))
patched = 0
for key, blocks in data.items():
    for b in blocks:
        if b.get("t") == "img" and b.get("src") in remap:
            b["src"] = remap[b["src"]]
            patched += 1
json.dump(data, open(CONTENT, "w", encoding="utf-8"), ensure_ascii=False, separators=(",", ":"))

print(f"images: {len(remap)}  patched refs: {patched}")
print(f"image bytes: {before//1024} KB -> {after//1024} KB  (saved {(before-after)//1024} KB)")
