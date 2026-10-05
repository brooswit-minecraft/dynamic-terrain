#!/usr/bin/env python3
"""Generate blockstate, block-model, item-model, lang and tag JSON for the
layered blocks. Re-run after changing MATERIALS or the layer count; output is
committed so the build needs no datagen run.

Usage: python3 scripts/gen-layered-resources.py
"""
import json
from pathlib import Path

MOD = "dynamicterrain"
MAX_LAYERS = 16
# block name -> (display name, texture)
MATERIALS = {"layered_dirt": ("Layered Dirt", "minecraft:block/dirt")}

root = Path(__file__).resolve().parent.parent / "src/main/resources/assets" / MOD
data = Path(__file__).resolve().parent.parent / "src/main/resources/data"


def write(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n")


def block_model(texture, ceiling, n):
    lo, hi = (MAX_LAYERS - n, MAX_LAYERS) if ceiling else (0, n)
    side_uv = [0, 0, 16, n] if ceiling else [0, MAX_LAYERS - n, 16, 16]
    face = lambda uv, cull=None: {"uv": uv, "texture": "#all", **({"cullface": cull} if cull else {})}
    return {
        "parent": "minecraft:block/block",
        "textures": {"all": texture, "particle": texture},
        "elements": [{
            "from": [0, lo, 0], "to": [16, hi, 16],
            "faces": {
                "down": face([0, 0, 16, 16], "down" if not ceiling or n == MAX_LAYERS else None),
                "up": face([0, 0, 16, 16], "up" if ceiling or n == MAX_LAYERS else None),
                "north": face(side_uv), "south": face(side_uv),
                "west": face(side_uv), "east": face(side_uv),
            },
        }],
    }


lang = {}
for name, (display, texture) in MATERIALS.items():
    variants = {}
    for ceiling in (False, True):
        anchor = "ceiling" if ceiling else "floor"
        for n in range(1, MAX_LAYERS + 1):
            model = f"{name}_{anchor}_{n}"
            write(root / "models/block" / f"{model}.json", block_model(texture, ceiling, n))
            variants[f"anchor={anchor},layers={n}"] = {"model": f"{MOD}:block/{model}"}
    write(root / "blockstates" / f"{name}.json", {"variants": variants})
    write(root / "models/item" / f"{name}.json", {"parent": f"{MOD}:block/{name}_floor_8"})
    lang[f"block.{MOD}.{name}"] = display

write(root / "lang/en_us.json", lang)
write(data / "minecraft/tags/block/mineable/shovel.json",
      {"values": [f"{MOD}:{n}" for n in MATERIALS]})
