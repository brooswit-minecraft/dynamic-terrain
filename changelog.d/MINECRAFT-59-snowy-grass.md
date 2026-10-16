bump: minor

### Added
- Layered grass now tracks vanilla's `snowy` block property: a floor-anchored layer under a block in `#minecraft:snow` (a snow layer or snow block) renders the frosted `grass_block_snow` side texture with no grass tint, exactly like vanilla's `grass_block`. Updates live via neighbor changes (placing/removing snow above), and is computed correctly on grading, smoothing and moisture/damage transitions. Ceiling-anchored grass stays `snowy=false` (nothing meaningfully sits "above" it). No new textures: reuses vanilla's `grass_block_snow`.
