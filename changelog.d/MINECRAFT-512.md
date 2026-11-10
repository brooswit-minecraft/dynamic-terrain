bump: major

### BREAKING
- Entity-footfall erosion (`EntityErosion`, `EntityMath`, and the `entityErosion` config key)
  moved to the standalone Layered Slope Dynamic Paths mod, which depends on Layered Slopes
  directly and no longer needs Dynamic Terrain at all. Dynamic Terrain no longer registers or
  runs walking/landing footfall erosion; install Layered Slope Dynamic Paths alongside Layered
  Slopes to keep mobs/players wearing down the ground they walk and land on. Manual grading,
  water/heat/vehicle erosion, cave-ins and driving-surface physics are unaffected.

### Removed
- `DynamicTerrainConfig.ENTITY_EROSION` / the `entityErosion` config key.
