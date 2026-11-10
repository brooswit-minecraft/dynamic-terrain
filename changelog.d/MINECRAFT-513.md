bump: major

### BREAKING
- Water erosion (`WaterErosion`/`WaterMath`, the `/dtwater` debug command, and the
  `waterSamplesPerSecond` config key) moved to the standalone
  [Layered Slopes Flowing Fluids](https://github.com/brooswit-minecraft/layered-slopes-flowing-fluids)
  mod, which depends on Layered Slopes directly and no longer needs Dynamic Terrain at all.
  Dynamic Terrain no longer registers or runs water erosion; install Layered Slopes Flowing
  Fluids alongside Layered Slopes to keep flowing-fluid layer erosion working. An existing
  server where water erosion was genuinely running (or genuinely off) under this mod's own
  `erosionEnabled`+`waterSamplesPerSecond` is migrated forward as Layered Slopes Flowing Fluids'
  own defaults on its first config write, so the upgrade does not silently flip that choice (see
  Layered Slopes Flowing Fluids' own `MigrationPolicy`/`LegacyConfigMigration`). Heat erosion,
  entity erosion, manual grading, cave-ins and driving-surface physics are unaffected.

### Removed
- `WaterErosion`, `WaterMath`, and `DynamicTerrainConfig.WATER_SAMPLES_PER_SECOND` / the
  `waterSamplesPerSecond` config key.
- The `/dtwater` debug command.
