bump: minor

### Added
- Entity erosion (needs `erosionEnabled = true`, default off): living entities near a player that are walking on the ground report roughly mass x speed to erode() for the block under their feet. Herds and busy paths slowly wear soft ground into layers and gravel, and standing still does nothing.
- Server config `entityErosion` (default true, only matters when erosion is enabled).
