bump: major

### BREAKING
- Worldgen-only surface smoothing (`SmoothSurfaceFeature`, the `worldgenSmoothing` config key,
  and the `smooth_surface` feature/configured-feature/placed-feature/biome-modifier resources)
  moved to the standalone Smooth Slopes mod, which depends on Layered Slopes directly and no
  longer needs Dynamic Terrain at all. Dynamic Terrain no longer registers or runs this feature;
  install Smooth Slopes alongside Layered Slopes to keep newly generated terrain smoothed. An
  existing `worldgenSmoothing = false` in `config/dynamicterrain-server.toml` is migrated forward
  as Smooth Slopes' own default on its first config write, so an explicit opt-out is not silently
  re-enabled by the upgrade (see Smooth Slopes' own `MigrationPolicy`/`LegacyConfigMigration`).
  Manual grading, erosion, cave-ins and driving-surface physics are unaffected.

### Removed
- `DynamicTerrainConfig.WORLDGEN_SMOOTHING` / the `worldgenSmoothing` config key.
