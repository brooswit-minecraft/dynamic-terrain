bump: minor

### Added
- Water erosion (needs `erosionEnabled = true`, default off): each second, `waterSamplesPerSecond` random positions near each player are sampled, and a layered or erodible block next to water reports the contact to erode(). Moving water erodes much faster than still source water and shallower flow erodes less, so stream banks wear down into layers and gravel while ponds barely change. No commands needed.
- Server config `waterSamplesPerSecond` (default 128, 0 disables water erosion).
- Op-only debug command `/dtwater <x y z> <samples>` to run one sampling pass by hand.
