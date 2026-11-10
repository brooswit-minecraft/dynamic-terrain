bump: major

### BREAKING
- Manual layer grading (the right-click-with-pickaxe `GradingTool`) and its supporting
  pure-logic classes moved to the standalone Layered Slope Grading mod: `GradingTool`,
  `Smoothing`, `SmoothRules`, `Settling`, `LayeredMaterials` and `Heading`, now at
  `io.github.brooswitminecraft.layeredslopegrading`. Dynamic Terrain no longer registers the
  grading tool itself; it now requires Layered Slope Grading alongside Layered Slopes, and
  `Erosion.java` calls into the extracted `Smoothing`/`LayeredMaterials` classes with
  `disturb=true` exactly as before. Install Layered Slope Grading alongside Layered Slopes to
  keep manual grading and the `/dtsmooth` debug command available (both unchanged in behavior
  and name). Worldgen smoothing, erosion, cave-ins and driving-surface physics are unaffected.

### Removed
- `Heading`/`SmoothRules`/`Settling`/`Smoothing`/`LayeredMaterials`/`GradingTool` from this
  repo's own package (now provided by Layered Slope Grading).
