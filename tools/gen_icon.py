#!/usr/bin/env python3
"""Build an adaptive launcher icon + splash logo from the red-ribbon logo."""
import os, shutil
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app/src/main/res")

# source: the original red-ribbon logo (square, highest density available)
src = Image.open(os.path.join(RES, "mipmap-xxxhdpi/ic_launcher.png")).convert("RGBA")

# Adaptive foreground is 108dp; the logo sits in the ~66dp safe zone.
# px = 108 * densityFactor
densities = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
SAFE = 0.62  # fraction of the 108dp canvas the logo occupies

for name, f in densities.items():
    canvas_px = int(round(108 * f))
    logo_px = int(round(canvas_px * SAFE))
    logo = src.resize((logo_px, logo_px), Image.LANCZOS)
    fg = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    off = (canvas_px - logo_px) // 2
    fg.paste(logo, (off, off), logo)
    out_dir = os.path.join(RES, f"mipmap-{name}")
    fg.save(os.path.join(out_dir, "ic_fg.png"))

# Splash logo: a plain raster (painterResource can't read the adaptive XML that
# shadows @mipmap/ic_launcher on API 26+). Use the round logo on transparency.
os.makedirs(os.path.join(RES, "drawable-nodpi"), exist_ok=True)
round_src = os.path.join(RES, "mipmap-xxxhdpi/ic_launcher_round.png")
shutil.copyfile(round_src, os.path.join(RES, "drawable-nodpi/logo.png"))
print("generated ic_fg.png (5 densities) + drawable-nodpi/logo.png")
