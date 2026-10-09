bump: patch

### Fixed
- Ground mobs no longer freeze on layered/sloped terrain (MINECRAFT-201). `LayeredBlock` now overrides `isPathfindable` (matching vanilla `SnowLayerBlock`'s pattern): LAND is walkable below 8 of 16 layers, and WATER/AIR are never pathfindable through it. Previously the inherited default treated almost every partial-layer height as pathfinder-`OPEN` instead of `WALKABLE`, so most of a smoothed slope had no valid path/start node.
