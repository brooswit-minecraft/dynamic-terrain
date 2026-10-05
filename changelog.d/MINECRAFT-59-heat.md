bump: minor

### Added
- Heat erosion (needs `erosionEnabled = true`, default off): each second, `heatSamplesPerSecond` random positions near each player are sampled, and a layered or erodible block touching lava reports heat to erode(). Over long periods lava regions round and degrade the geology beside them (stone to cobblestone to gravel). This is the V1 heat caller and senses lava directly; a Dynamic Atmosphere heat field can feed the same erode() later.
- Server config `heatSamplesPerSecond` (default 128, 0 disables heat erosion).
- Op-only debug command `/dtheat <x y z> <samples>`.
