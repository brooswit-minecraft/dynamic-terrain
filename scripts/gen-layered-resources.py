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
# block name -> (display name, texture, tool)
MATERIALS = {
    "layered_dirt": ("Layered Dirt", "minecraft:block/dirt", "shovel"),
    "layered_sand": ("Layered Sand", "minecraft:block/sand", "shovel"),
    "layered_gravel": ("Layered Gravel", "minecraft:block/gravel", "shovel"),
}
# Grass-topped materials: floor-anchored layers show a biome-tinted grass top and side overlay over
# dirt; ceiling-anchored layers hang as plain dirt (grass does not grow downward).
GRASS = {
    "layered_grass": ("Layered Grass", "shovel"),
}

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


def grass_model(ceiling, n):
    if ceiling:
        return block_model("minecraft:block/dirt", ceiling, n)
    # The grass strip is the top of the side texture, so a layer shows the top n rows: grass over dirt.
    side_uv = [0, 0, 16, n]
    top_cull = "up" if n == MAX_LAYERS else None
    base = {
        "from": [0, 0, 0], "to": [16, n, 16],
        "faces": {
            "down": {"uv": [0, 0, 16, 16], "texture": "#bottom", "cullface": "down"},
            "up": {"uv": [0, 0, 16, 16], "texture": "#top", "tintindex": 0, **({"cullface": top_cull} if top_cull else {})},
            "north": {"uv": side_uv, "texture": "#side"}, "south": {"uv": side_uv, "texture": "#side"},
            "west": {"uv": side_uv, "texture": "#side"}, "east": {"uv": side_uv, "texture": "#side"},
        },
    }
    overlay = {
        "from": [0, 0, 0], "to": [16, n, 16],
        "faces": {d: {"uv": side_uv, "texture": "#overlay", "tintindex": 0} for d in ("north", "south", "west", "east")},
    }
    return {
        "parent": "minecraft:block/block",
        # The overlay texture has transparent pixels; in the solid layer they render black.
        "render_type": "minecraft:cutout_mipped",
        "textures": {"particle": "minecraft:block/dirt", "bottom": "minecraft:block/dirt",
                     "top": "minecraft:block/grass_block_top", "side": "minecraft:block/grass_block_side",
                     "overlay": "minecraft:block/grass_block_side_overlay"},
        "elements": [base, overlay],
    }


lang = {}
for name, (display, tool) in GRASS.items():
    variants = {}
    for ceiling in (False, True):
        anchor = "ceiling" if ceiling else "floor"
        for n in range(1, MAX_LAYERS + 1):
            model = f"{name}_{anchor}_{n}"
            write(root / "models/block" / f"{model}.json", grass_model(ceiling, n))
            variants[f"anchor={anchor},layers={n}"] = {"model": f"{MOD}:block/{model}"}
    write(root / "blockstates" / f"{name}.json", {"variants": variants})
    write(root / "models/item" / f"{name}.json", {"parent": f"{MOD}:block/{name}_floor_8"})
    lang[f"block.{MOD}.{name}"] = display

for name, (display, texture, tool) in MATERIALS.items():
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
for tool in ("shovel", "pickaxe"):
    names = [n for n, spec in MATERIALS.items() if spec[2] == tool] + [n for n, spec in GRASS.items() if spec[1] == tool]
    if names:
        write(data / f"minecraft/tags/block/mineable/{tool}.json", {"values": [f"{MOD}:{n}" for n in names]})
