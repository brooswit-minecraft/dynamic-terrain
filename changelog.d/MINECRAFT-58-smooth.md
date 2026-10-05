bump: minor

### Added
- `Smoothing.smooth(level, pos, direction)`: moves exactly one sixteenth of material from a layered block, or a full dirt/sand/gravel block, into the neighbouring block. An empty destination becomes a one-layer block, a same-material layered destination gains a layer, anything else is rejected. Ceiling layers only spread where they stay ceiling-anchored.
- Op-only debug command `/dtsmooth <x y z> <direction>` to call smooth() by hand until the grading tool lands.
