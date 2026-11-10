bump: major

### BREAKING
- The layered block family (`LayeredBlock`/`LayeredGrassBlock`, `LayerMath`,
  `Anchor`, their blockstates/models/lang/recipes/mineable tags) moved out
  of this mod into the standalone
  [Layered Slopes](https://github.com/brooswit-minecraft/layered-slopes)
  mod, which this mod now requires. Registry names kept the pre-split
  `dynamicterrain` namespace, so an existing save keeps resolving every
  `dynamicterrain:layered_*` block/item once Layered Slopes is installed
  alongside this mod; installing this version *without* Layered Slopes is
  not a supported pairing.

### Changed
- Smoothing, erosion, cave-ins and the grading tool now consume the
  extracted block API (`LayeredMaterials` registration moved to
  `DynamicTerrainMod`, referencing `LayeredSlopesMod`'s registered blocks)
  instead of owning the blocks themselves. Grading tools, manual
  smoothing/terrain editing, world generation, erosion/cave-ins and
  driving-surface physics are unchanged and stay in this mod.
