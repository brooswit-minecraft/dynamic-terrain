bump: minor

### Added
- **Worldgen smoothing** (config `worldgenSmoothing`, **on by default**): newly generated overworld chunks have their 1-block surface steps replaced by fractional ramps of layered blocks (layered grass, dirt, sand, gravel and the stone family, matching the natural top block). Only plain natural ground is touched: columns with trees, plants, snow, water, structures, roads or farmland are left alone, a column never moves by a whole block, and chunk edges agree because each chunk works from the same natural heights. Slopes steeper than about one block per block cannot be smoothed (a 45-degree staircase is already a straight line).
- **New chunks only.** Existing chunks never change, so at the border between explored and newly generated terrain the old side keeps its steps. Works alongside Tectonic. In a vanilla-noise test world, whole-block steps between neighbouring columns fell from 41% to 24%; with Tectonic, which is already gentle, from 3.8% to 3.0% in the same test area.
- Op-only debug command `/dtheightstats <x> <z> <radius>` measures the surface steps of loaded terrain.
