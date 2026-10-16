bump: minor

### Added
- Per-texel analysis of a block's canonical texture (chroma, luminance contrast, X/Y local variation), read straight off the classpath so it always reflects the asset the jar shipped with rather than an active resource pack.
- `Surfaces.contactPatch(...)`: samples that per-texel evidence across the texels a tire contact patch actually crosses on a given face this physics step, oriented into the face's own texture coordinates so directional grain (e.g. wood) can emerge.
- Results are cached per texture and face set; the cache is invalidated on resource reload.
- `/dttexture <pos> <face>` debug command to inspect the evidence at a block.
