bump: minor

### Added
- Vehicle surface model, automatic level: `Surfaces.at(level, pos)` returns `SurfaceProperties` (grip, roughness, rollingResistance, deformability, all 0..1) inferred from the block's sound group (material prior), vanilla friction, hardness and blast resistance. Calibrated by unit tests against vanilla surfaces (ice low-grip, sand highly deformable, gravel rough, grass irregular, stone and deepslate consistent pavement, obsidian reads as hard pavement rather than an outlier). Vehicles ask this; it never changes terrain.
- Op-only debug command `/dtsurface <x y z>` prints a block's properties.
