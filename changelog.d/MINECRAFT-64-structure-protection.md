bump: patch

### Fixed
- Worldgen smoothing (MINECRAFT-64) now checks a column against the chunk's own generated structure starts/references before touching it, in addition to the existing block-based heuristic. Previously a column was protected only if something was placed directly on top of it, so bare natural ground inside a village or other structure's footprint (an open plaza, or the terrain-adaptation "beard" next to a door) could still be smoothed. The check stays within chunks already in cache, so it can never force-load a chunk outside the generation step's region.
