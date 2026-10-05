# Dynamic Terrain

Early alpha for NeoForge 1.21.1.

## What's in it

- **Layered dirt, sand and gravel** in 1/16-block layers, floor-anchored (grow up) or ceiling-anchored (hang down). Stack up to 16 layers.
- **Shovel grading:** right-click a block with a shovel to pull one sixteenth of it toward you.
- **`smooth(direction)`:** the deterministic primitive behind grading.
- **Transition registry:** other mods and datapacks can declare how their blocks respond to generic environmental actions (`data/<namespace>/dynamicterrain/transitions/<name>.json`).
- Op-only debug commands: `/dtsmooth` and `/dttransition`.

Layers do not yet fall or erode. Erosion and structural support are planned and will be off by default.

Source and issues: https://github.com/brooswit-minecraft/dynamic-terrain
