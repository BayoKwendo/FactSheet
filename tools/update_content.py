#!/usr/bin/env python3
"""
Prepend an "updated for 2025" section to key clinical pages, reflecting current
WHO / Kenya NASCOP HIV guidance. Additive and idempotent: existing content is
preserved; running twice does not duplicate (keyed by a marker heading).

Sources (Nov 2025):
- WHO/IAS-USA 2024: TLD (TDF+3TC+DTG) preferred first-line ART.
- U=U scientific consensus (WHO 2023, CDC, UNAIDS): undetectable = untransmittable.
- WHO PrEP 2025: oral TDF/FTC, CAB-LA, dapivirine ring, twice-yearly lenacapavir.
- WHO PEP 2024: start <72h, 28-day course, 3-drug preferred.
- Kenya ARV Guidelines 2022 (NASCOP): test-and-treat, same-day ART.
- UNAIDS 95-95-95 targets; WHO strongly recommends HIV self-testing.
"""
import json, os

CONTENT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                       "app/src/main/assets/gen/content.json")
MARKER = "Latest guidance"
GREEN = "#1d7a10"
BLUE = "#1a4fd6"

def span(text, b=False, c=None):
    d = {"text": text}
    if b: d["b"] = 1
    if c: d["c"] = c
    return d

def h(text, color=GREEN):
    return {"t": "h", "lvl": 2, "s": [span(text, b=True, c=color)]}

def p(*spans):
    return {"t": "p", "s": list(spans)}

def li(*spans):
    return {"t": "li", "s": list(spans)}

# Updated sections, keyed by content key. Each starts with the MARKER heading.
UPDATES = {
    "whatishiv": [
        h(MARKER),
        p(span("HIV is now a manageable long-term condition. People on effective "
               "treatment live long, healthy lives.")),
        p(span("Test and treat: "), span("anyone who tests HIV-positive starts "
               "antiretroviral therapy (ART) the same day, whatever their CD4 count.")),
        p(span("U = U — Undetectable = Untransmittable. ", b=True, c=BLUE),
          span("A person on ART whose viral load stays undetectable "),
          span("cannot pass HIV to sexual partners.", b=True),
          span(" This is global scientific consensus (WHO, CDC, UNAIDS).")),
    ],
    "fact": [
        h("Current first-line treatment"),
        p(span("The preferred first-line regimen is "),
          span("TLD", b=True),
          span(" — one pill, once a day, combining Tenofovir (TDF), Lamivudine (3TC) "
               "and Dolutegravir (DTG).")),
        p(span("It is recommended by WHO and Kenya's NASCOP guidelines for adults, "
               "adolescents, pregnant women and people with hepatitis B.")),
        p(span("ART is started the same day as diagnosis whenever possible. "
               "Dolutegravir-based ART keeps the virus suppressed in over 95% of "
               "people who stay in care.")),
    ],
    "therapy": [
        h("The goal: an undetectable viral load"),
        p(span("The aim of treatment is to lower the amount of HIV in the blood to "
               "an "), span("undetectable", b=True), span(" level (under 50 copies/mL).")),
        p(span("Staying undetectable keeps your immune system strong and means you "
               "cannot transmit HIV sexually (U=U).")),
        p(span("Global 95-95-95 goal: ", b=True),
          span("95% of people with HIV knowing their status, 95% of them on ART, and "
               "95% of those virally suppressed.")),
    ],
    "stage": [
        h(MARKER),
        p(span("The term "), span("“advanced HIV disease”", b=True),
          span(" is now used instead of “AIDS”. With early treatment, most "
               "people never reach this stage.")),
        p(span("Everyone starts ART immediately at diagnosis, so HIV is usually "
               "controlled long before the immune system is damaged.")),
    ],
    "cdover": [
        h(MARKER),
        p(span("Treatment no longer waits for a low CD4 count. "),
          span("Everyone living with HIV starts ART straight away, whatever their CD4.",
               b=True)),
        p(span("CD4 is still useful to check immune health and the risk of "
               "opportunistic infections, especially at the start of care.")),
    ],
    "preps": [
        h("PrEP options"),
        p(span("PrEP (Pre-Exposure Prophylaxis) is HIV medicine taken by HIV-negative "
               "people to stay negative. Today there are several options:")),
        li(span("Daily oral PrEP", b=True), span(" (TDF/FTC) — one pill every day.")),
        li(span("Event-driven “2-1-1” oral PrEP", b=True),
           span(" — an option for men who have sex with men, around specific events.")),
        li(span("Injectable cabotegravir (CAB-LA)", b=True),
           span(" — an injection once every 2 months.")),
        li(span("Dapivirine vaginal ring", b=True),
           span(" — a woman-controlled ring replaced monthly.")),
        li(span("Lenacapavir (LEN)", b=True),
           span(" — a twice-a-year injection, recommended by WHO and "
                "highly effective in trials.")),
        p(span("Choose PrEP with a health worker, and keep using condoms and regular "
               "HIV testing alongside it.")),
    ],
    "peps": [
        h("PEP: emergency HIV prevention"),
        p(span("Start PEP within 72 hours", b=True, c=BLUE),
          span(" of a possible exposure — the sooner the better — and take it for "),
          span("28 days", b=True), span(". A three-drug regimen is preferred.")),
        p(span("PEP is for emergencies such as condom failure, sex without protection, "
               "sexual assault or a needle-stick injury. It is not a substitute for "
               "PrEP or condoms.")),
    ],
    "hos": [
        h(MARKER),
        p(span("Taking your ART every day keeps your viral load undetectable — that "
               "keeps you healthy and means you "),
          span("cannot pass HIV to sexual partners (U=U).", b=True)),
    ],
    "hivintr": [
        h(MARKER),
        p(span("HIV self-testing", b=True),
          span(" is now widely available: you can test yourself at home with a simple "
               "oral-fluid or finger-prick kit, then confirm any reactive result at a "
               "clinic.")),
        p(span("If you test positive, treatment starts the same day — and effective "
               "treatment leads to an undetectable, untransmittable viral load (U=U).")),
    ],
    "hivtest": [
        h(MARKER),
        p(span("You can now test at home with an "),
          span("HIV self-test kit", b=True),
          span(" (oral-fluid or finger-prick), as well as at clinics, pharmacies and "
               "through community outreach.")),
        p(span("A reactive self-test is always confirmed with a follow-up test at a "
               "health facility before any diagnosis.")),
    ],
    "sex_safe": [
        h(MARKER),
        p(span("Modern HIV prevention combines several tools: "),
          span("condoms, PrEP for HIV-negative people, and treatment (U=U) for people "
               "living with HIV.", b=True)),
    ],
}

def main():
    data = json.load(open(CONTENT, encoding="utf-8"))
    updated = skipped = 0
    for key, section in UPDATES.items():
        if key not in data:
            print("  missing key:", key); continue
        blocks = data[key]
        already = any(b.get("t") == "h" and
                      "".join(s["text"] for s in b.get("s", [])).startswith(MARKER[:14])
                      and MARKER[:6] in "".join(s["text"] for s in b.get("s", []))
                      for b in blocks[:1])
        # idempotency: check if first block already is one of our headings
        first_txt = "".join(s["text"] for s in blocks[0].get("s", [])) if blocks else ""
        if first_txt.startswith(("Latest guidance", "Current first-line",
                                 "The goal:", "PrEP options", "PEP: emergency")):
            skipped += 1; continue
        data[key] = section + blocks
        updated += 1
    json.dump(data, open(CONTENT, "w", encoding="utf-8"),
              ensure_ascii=False, separators=(",", ":"))
    print(f"updated pages: {updated}  skipped(existing): {skipped}")

if __name__ == "__main__":
    main()
