#!/usr/bin/env python3
"""
Finalize the generated content: clean conversion artifacts and prepend concise
"Latest guidance" sections reflecting current GLOBAL standards (WHO, CDC,
UNAIDS; also Kenya NASCOP). Additive — original text is preserved below each
note. Fully idempotent: every inserted block is tagged {"gen":1} and removed
before re-applying, so this can run on freshly generated or already-updated data.

Key facts reflected (reviewed Nov 2026):
- Treat-all / same-day ART; preferred first-line TLD (TDF+3TC+DTG), an INSTI-based
  regimen. (WHO 2023/24, IAS-USA 2024.)
- U=U: a person on ART with a sustained undetectable viral load does not transmit
  HIV sexually. (WHO 2023, CDC, UNAIDS.)
- PrEP options: daily oral TDF/FTC, event-driven 2-1-1, injectable CAB-LA (2-monthly),
  dapivirine ring, twice-yearly lenacapavir. (WHO 2022/2025.)
- PEP within 72h, 28-day 3-drug course. (WHO 2024.)
- PMTCT: lifelong ART for all pregnant/breastfeeding women; MTCT <1% with
  suppression. Breastfeeding on suppressive ART recommended where formula is not
  safe/sustainable (WHO), formula typical in high-income settings.
- HIV self-testing; modern 4th-gen tests detect within ~2-6 weeks.
- Hepatitis C now curable with direct-acting antivirals; HPV vaccine key prevention.
"""
import json, os

CONTENT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                       "app/src/main/assets/gen/content.json")
GREEN = "#1d7a10"
BLUE = "#1a4fd6"

def s(text, b=False, c=None):
    d = {"text": text}
    if b: d["b"] = 1
    if c: d["c"] = c
    return d

def H(text, color=GREEN):
    return {"t": "h", "lvl": 2, "s": [s(text, b=True, c=color)], "gen": 1}

def P(*spans):
    return {"t": "p", "s": list(spans), "gen": 1}

def L(*spans):
    return {"t": "li", "s": list(spans), "gen": 1}

# Reusable U=U sentence.
UU = lambda: P(s("U = U — Undetectable = Untransmittable. ", b=True, c=BLUE),
               s("Someone on HIV treatment with a sustained undetectable viral load "
                 "cannot pass HIV to sexual partners. This is backed by global "
                 "scientific consensus (WHO, CDC, UNAIDS)."))

UPDATES = {
    # ---- core overview / treatment ----
    "whatishiv": [
        H("Latest guidance"),
        P(s("HIV is now a manageable long-term condition — people on effective "
            "treatment live long, healthy lives.")),
        P(s("Treat all: ", b=True), s("anyone who tests HIV-positive starts "
          "antiretroviral therapy (ART) the same day, worldwide, whatever their CD4 count.")),
        UU(),
    ],
    "fact": [
        H("Current treatment"),
        P(s("The preferred first-line regimen worldwide is "), s("TLD", b=True),
          s(" — one pill once a day combining Tenofovir (TDF), Lamivudine (3TC) and "
            "Dolutegravir (DTG). Dolutegravir is an integrase inhibitor (INSTI), the "
            "newest and now most-used drug class.")),
        P(s("Everyone starts ART immediately at diagnosis (treat-all), not only when "
            "CD4 is low. Dolutegravir-based ART suppresses the virus in over 95% of "
            "people who stay in care.")),
    ],
    "ttt": [
        H("Current treatment"),
        P(s("Alongside NRTIs, NNRTIs and protease inhibitors, "),
          s("integrase inhibitors (INSTIs) such as dolutegravir", b=True),
          s(" are now a core class. The global first-line regimen is TLD "
            "(TDF + 3TC + DTG).")),
        P(s("ART is started for everyone at diagnosis, regardless of CD4 count or "
            "symptoms.")),
    ],
    "therapy": [
        H("The goal: an undetectable viral load"),
        P(s("Treatment aims to lower HIV in the blood to an "), s("undetectable", b=True),
          s(" level (under 50 copies/mL). Staying undetectable keeps you healthy and "
            "means you cannot transmit HIV sexually (U=U).")),
        P(s("Global 95-95-95 goal: ", b=True),
          s("95% of people with HIV diagnosed, 95% of them on ART, 95% of those "
            "virally suppressed.")),
    ],
    "stage": [
        H("Latest guidance"),
        P(s("These stages describe "), s("untreated", b=True),
          s(" HIV. Today everyone starts ART immediately at diagnosis, so most people "
            "never progress and stay well for decades.")),
        P(s("The term "), s("“advanced HIV disease”", b=True),
          s(" is now used instead of “AIDS”.")),
    ],
    "cdover": [
        H("Latest guidance"),
        P(s("Treatment no longer waits for a low CD4 count — "),
          s("everyone living with HIV starts ART straight away, whatever their CD4.", b=True)),
        P(s("CD4 is still used to check immune health and the risk of opportunistic "
            "infections, especially when starting care.")),
    ],
    "defintr": [
        H("Latest guidance"),
        P(s("ART does more than protect your immune system: keeping the virus "
            "undetectable also means HIV cannot be passed on sexually (U=U).")),
    ],
    "numbe": [
        H("Latest guidance"),
        P(s("Everyone diagnosed starts ART immediately (treat-all).")),
        UU(),
    ],
    # ---- adherence ----
    "hos": [
        H("Latest guidance"),
        P(s("Taking ART every day keeps your viral load undetectable — that keeps you "
            "healthy and means you "),
          s("cannot pass HIV to sexual partners (U=U).", b=True)),
    ],
    # ---- testing ----
    "hivintr": [
        H("Latest guidance"),
        P(s("HIV self-testing", b=True),
          s(" is now available worldwide — test yourself at home with an oral-fluid or "
            "finger-prick kit, then confirm any reactive result at a clinic.")),
        P(s("If positive, treatment starts the same day and leads to an undetectable, "
            "untransmittable viral load (U=U).")),
    ],
    "hivtest": [
        H("Latest guidance"),
        P(s("You can now test at home with an "), s("HIV self-test kit", b=True),
          s(", as well as at clinics, pharmacies and through community outreach. A "
            "reactive self-test is always confirmed at a health facility.")),
    ],
    "socii": [
        H("Latest guidance"),
        P(s("Modern 4th-generation antigen/antibody tests detect most infections "
            "within about 2-6 weeks", b=True),
          s(" — far sooner than older tests. Nucleic-acid (viral load) tests detect "
            "even earlier. HIV self-testing is also widely available.")),
    ],
    "hivb": [
        H("Latest guidance"),
        P(s("The window period is now shorter: 4th-generation tests detect most "
            "infections within 2-6 weeks. A negative result after a possible exposure "
            "should still be confirmed once the window has passed (about 3 months for "
            "antibody-only rapid tests).")),
    ],
    "hiv": [
        H("Why test"),
        P(s("Testing is the gateway to same-day treatment if positive (reaching an "
            "undetectable, untransmittable viral load, U=U) or to prevention such as "
            "PrEP if negative. Free HIV self-test kits are widely available.")),
    ],
    "hivwho": [
        H("Latest guidance"),
        P(s("Testing is recommended for anyone who wants to know their status, and "
            "especially people with a new or multiple partners, another STI, TB, or who "
            "could benefit from PrEP. "),
          s("HIV self-test kits", b=True), s(" make testing easy and private.")),
    ],
    "hivv": [
        H("Latest guidance"),
        P(s("If one partner is HIV-positive, they can take ART to reach an undetectable "
            "viral load so HIV is not passed on (U=U), while the HIV-negative partner can "
            "use PrEP. With ART in pregnancy, nearly all babies are born HIV-free.")),
    ],
    # ---- prevention ----
    "preps": [
        H("PrEP options"),
        P(s("PrEP (Pre-Exposure Prophylaxis) is HIV medicine taken by HIV-negative "
            "people to stay negative. Options now include:")),
        L(s("Daily oral PrEP", b=True), s(" (TDF/FTC) — one pill every day.")),
        L(s("Event-driven “2-1-1” oral PrEP", b=True),
          s(" — around specific events, an option for men who have sex with men.")),
        L(s("Injectable cabotegravir (CAB-LA)", b=True), s(" — an injection every 2 months.")),
        L(s("Dapivirine vaginal ring", b=True), s(" — a woman-controlled monthly ring.")),
        L(s("Lenacapavir (LEN)", b=True),
          s(" — a twice-a-year injection, recommended by WHO and highly effective in trials.")),
        P(s("Use PrEP with condoms and regular HIV testing.")),
    ],
    "rt": [
        H("PrEP options"),
        P(s("Besides daily oral PrEP, newer options are now recommended: "),
          s("injectable cabotegravir (CAB-LA) every 2 months, the dapivirine vaginal "
            "ring, event-driven 2-1-1 oral PrEP, and twice-yearly lenacapavir.", b=True)),
    ],
    "rr": [
        H("PrEP and PEP today"),
        P(s("PEP must start within 72 hours", b=True, c=BLUE),
          s(" of a possible exposure and is taken for 28 days. PrEP now also includes "
            "long-acting injectable options (CAB-LA, lenacapavir) and the dapivirine ring.")),
        UU(),
    ],
    "peps": [
        H("PEP: emergency HIV prevention"),
        P(s("Start PEP within 72 hours", b=True, c=BLUE),
          s(" of a possible exposure — the sooner the better — and take it for "),
          s("28 days", b=True), s(". A three-drug regimen is preferred.")),
        P(s("PEP is for emergencies (condom failure, sex without protection, sexual "
            "assault, needle-stick). It is not a substitute for PrEP or condoms.")),
    ],
    "sex_safe": [
        H("Latest guidance"),
        P(s("Modern HIV prevention combines tools: "),
          s("condoms, PrEP for HIV-negative people, PEP after an emergency, and "
            "treatment (U=U) for people living with HIV.", b=True)),
    ],
    "abstience": [
        H("Latest guidance"),
        P(s("Prevention today goes beyond abstinence, fidelity and condoms to include "),
          s("PrEP, PEP, HIV self-testing, voluntary medical male circumcision, and U=U "
            "(effective treatment means no sexual transmission).", b=True)),
    ],
    "cds": [
        H("Latest guidance"),
        P(s("Use a new condom for each act of sex — there is no need to change it on a "
            "timed schedule. Add water-based lubricant to reduce breakage.")),
        P(s("Condoms are vital, but HIV prevention also includes PrEP for HIV-negative "
            "people, PEP after an emergency, and U=U.")),
    ],
    "transmoccur": [
        H("Latest guidance"),
        P(s("The sexual transmission risks below apply to "), s("untreated", b=True),
          s(" HIV. A person on effective ART with an undetectable viral load does not "
            "transmit HIV sexually (U=U).")),
    ],
    "relationship": [
        H("Latest guidance"),
        UU(),
        P(s("The HIV-negative partner can also use PrEP for extra protection. Couples "
            "where the positive partner is undetectable can conceive safely.")),
    ],
    # ---- pregnancy / PMTCT / infant feeding ----
    "su": [
        H("Latest guidance"),
        P(s("Older AZT (zidovudine)-only regimens are obsolete.", b=True),
          s(" Today every pregnant woman with HIV takes lifelong combination ART "
            "(dolutegravir-based), started the same day as diagnosis.")),
        P(s("With a sustained undetectable viral load, the chance of passing HIV to the "
            "baby falls below 1%. The baby receives short post-exposure medicine after "
            "birth per national protocol.")),
        P(s("Infant feeding: ", b=True),
          s("WHO recommends mothers on effective ART breastfeed (exclusively for the "
            "first 6 months, continuing up to 24 months) where formula is not safe and "
            "sustainable; formula feeding is typical in high-income settings. Either "
            "way, staying on ART keeps the risk very low.")),
    ],
    "breast": [
        H("Latest guidance"),
        P(s("WHO now recommends that mothers living with HIV "),
          s("breastfeed while taking effective ART with a suppressed viral load", b=True),
          s(" — exclusively for the first 6 months and continuing up to 24 months — in "
            "settings where formula is not safe and sustainable (such as much of "
            "sub-Saharan Africa).")),
        P(s("Maternal ART makes the risk of transmission through breast milk very low. "
            "In high-income settings, formula feeding is commonly advised. Avoid mixed "
            "feeding in the first months, and get help for cracked or bleeding nipples.")),
    ],
    "mtcp": [
        H("Latest guidance"),
        P(s("All pregnant women with HIV start lifelong ART immediately (treat-all). "
            "With a sustained undetectable viral load, mother-to-child transmission "
            "drops below 1%.")),
        P(s("On infant feeding, guidance depends on setting: ", b=True),
          s("WHO recommends breastfeeding while on suppressive ART where formula is not "
            "safe and sustainable; formula-only advice applies mainly to high-income "
            "settings.")),
    ],
    "conception": [
        H("Latest guidance"),
        P(s("When the partner living with HIV is on ART and undetectable, HIV is not "
            "passed on sexually (U=U), so many couples can conceive through timed "
            "condomless sex", b=True),
          s(" — sperm washing or self-insemination are usually not needed. PrEP for an "
            "HIV-negative partner adds protection.")),
        P(s("Mothers with HIV can breastfeed while on suppressive ART (see infant "
            "feeding guidance). Plan conception with your clinic.")),
    ],
    "contraceptive": [
        H("Latest guidance"),
        P(s("With today's dolutegravir-based ART, all modern contraceptives — including "
            "the implant — can be used.", b=True),
          s(" The old interaction concern was mainly with efavirenz. Condoms uniquely "
            "protect against both pregnancy and STIs, and HIV-negative people can also "
            "use PrEP.")),
    ],
    "family": [
        H("Latest guidance"),
        P(s("For emergency contraception, the "), s("copper IUD is the most effective", b=True),
          s(" (up to 5 days after sex); among pills, ulipristal acetate works better "
            "than levonorgestrel, especially at higher body weight.")),
    ],
    # ---- STIs / co-infections ----
    "sti": [
        H("Latest guidance"),
        P(s("The "), s("HPV vaccine", b=True),
          s(" is now a key prevention tool (routine for adolescents) and protects "
            "against cervical and other cancers. Partner-notification windows vary by "
            "infection and can extend beyond 60 days — follow clinical advice.")),
    ],
    "immune": [
        H("Latest guidance"),
        P(s("There is still no hepatitis C vaccine, but "),
          s("hepatitis C is now curable", b=True),
          s(" with short courses of direct-acting antiviral (DAA) tablets; treatment is "
            "recommended for everyone co-infected with HIV.")),
    ],
    # ---- youth / children sexual health ----
    "yt": [
        H("Latest guidance"),
        UU(),
        P(s("Condoms are still advised to prevent other STIs and unintended pregnancy.")),
    ],
    "sex": [
        H("Latest guidance"),
        P(s("For people on effective treatment with a sustained undetectable viral load, "
            "HIV cannot be passed to sexual partners (U=U). Condoms still protect against "
            "other STIs and pregnancy.")),
    ],
    "centr": [
        H("Latest guidance"),
        P(s("After sexual assault, start HIV post-exposure prophylaxis (PEP) as soon as "
            "possible and within 72 hours", b=True, c=BLUE),
          s("; emergency contraception and STI prevention are also provided. Go to a "
            "care centre or health facility urgently.")),
    ],
    # ---- statistics caveats ----
    "pop": [
        H("Note on figures"),
        P(s("Some statistics on this page are from the late 2000s/early 2010s. Kenya's "
            "adult HIV prevalence has since fallen to roughly 3.7-4%; see the latest "
            "UNAIDS and NASCOP/KENPHIA estimates for current global and national data.")),
    ],
    "popu": [
        H("Note on figures"),
        P(s("Some prevalence figures here are dated (mid/late 2000s). Current global and "
            "national estimates are lower — refer to the latest UNAIDS and NASCOP/KENPHIA "
            "data.")),
    ],
    # ---- relationships / dating / sexual health ----
    "dddd": [
        H("Latest guidance"),
        UU(),
        P(s("You can date and have a full sex life. On effective treatment you are "
            "undetectable and cannot pass HIV on; a partner can also use PrEP.")),
    ],
    "rap": [
        H("Latest guidance"),
        UU(),
    ],
    "patner": [
        H("Latest guidance"),
        UU(),
        P(s("An HIV-negative partner can use PrEP for added protection, and condoms "
            "still prevent other STIs and pregnancy.")),
    ],
    "healthy": [
        H("Latest guidance"),
        P(s("A healthy sex life is fully possible with HIV. Combine the tools that fit "
            "you: "),
          s("condoms, PrEP for HIV-negative partners, and treatment (U=U) for people "
            "living with HIV.", b=True)),
    ],
    "rb": [
        H("Latest guidance"),
        P(s("Condoms protect against both pregnancy and STIs. For HIV specifically, "
            "HIV-negative people can also use PrEP, and a partner on effective ART who "
            "is undetectable cannot transmit HIV (U=U).")),
    ],
    "risk_sex": [
        H("Latest guidance"),
        P(s("If you have had a higher-risk encounter, you can act: "),
          s("PEP within 72 hours", b=True, c=BLUE),
          s(" after a possible exposure, PrEP to stay protected going forward, HIV "
            "self-testing, and treatment (U=U) if you are living with HIV.")),
    ],
    "lub": [
        H("Latest guidance"),
        P(s("Use a new condom for each act of sex with water-based lubricant — there is "
            "no need to change it on a timed schedule. HIV prevention also includes PrEP "
            "and U=U.")),
    ],
    "stis": [
        H("Latest guidance"),
        P(s("The "), s("HPV vaccine", b=True),
          s(" is now a key prevention tool for adolescents and protects against cervical "
            "and other cancers. Many STIs are curable; get tested and treated early, and "
            "tell recent partners so they can be checked.")),
    ],
    "raaa": [
        H("Latest guidance"),
        P(s("HIV self-testing", b=True),
          s(" lets you test privately at home; a reactive result is confirmed at a "
            "clinic. If positive, ART starts the same day and leads to an undetectable, "
            "untransmittable viral load (U=U).")),
    ],
    "tean": [
        H("Latest guidance"),
        P(s("Young people on effective treatment who stay undetectable cannot pass HIV "
            "to sexual partners (U=U). Condoms still protect against other STIs and "
            "pregnancy.")),
    ],
    # ---- sexual assault (time-critical) ----
    "assaultincase": [
        H("Act quickly — within 72 hours"),
        P(s("Get to a health facility or care centre as soon as possible. ", b=True),
          s("HIV post-exposure prophylaxis (PEP) can prevent HIV if started within 72 "
            "hours", b=True, c=BLUE),
          s("; emergency contraception and STI prevention are also provided. Try not to "
            "wash or change clothes first, as this helps any evidence.")),
    ],
    "assaultdo": [
        H("Help them act within 72 hours"),
        P(s("Encourage and support them to reach a health facility or care centre "
            "urgently — "),
          s("HIV PEP works only if started within 72 hours", b=True, c=BLUE),
          s(", and emergency contraception and STI care are time-sensitive too.")),
    ],
    # ---- about the app ----
    "about_sa": [
        H("About this app"),
        P(s("FactSheet is a free guide to HIV, reproductive health and youth wellbeing. "
            "It has been rebuilt as a fast, offline-first native app and its health "
            "information is updated to reflect current global guidance (WHO, CDC, "
            "UNAIDS).")),
        P(s("The information is for education and does not replace advice from a "
            "qualified health worker.")),
    ],
}

def main():
    data = json.load(open(CONTENT, encoding="utf-8"))

    # 1. strip our previous insertions + conversion artifacts
    import re
    scroll = re.compile(r"scroll down", re.I)
    def artifact(b):
        if b.get("gen") == 1:
            return True
        t = "".join(x["text"] for x in b.get("s", []))
        if b.get("t") in ("p", "h") and scroll.search(t):
            return True
        return False
    for k in list(data.keys()):
        data[k] = [b for b in data[k] if not artifact(b)]

    # 2. prepend fresh guidance sections
    applied = missing = 0
    for key, section in UPDATES.items():
        if key not in data:
            print("  missing key:", key); missing += 1; continue
        data[key] = section + data[key]
        applied += 1

    json.dump(data, open(CONTENT, "w", encoding="utf-8"),
              ensure_ascii=False, separators=(",", ":"))
    print(f"updated pages: {applied}  missing keys: {missing}  total pages: {len(data)}")

if __name__ == "__main__":
    main()
