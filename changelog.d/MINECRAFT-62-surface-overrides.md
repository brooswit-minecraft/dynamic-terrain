bump: minor

### Added
- Surface override API (MINECRAFT-62 T21): `data/<namespace>/dynamicterrain/surface_overrides/<name>.json` declares a block's or a sound-family's `SurfaceProperties` (grip, roughness, rollingResistance, deformability), taking priority over `SurfaceMath`'s automatic inference. A per-block declaration wins over a family one. Files from every namespace merge, so other mods can opt a block in with a datapack file and no code — the same pattern as the existing `damage` transitions.
