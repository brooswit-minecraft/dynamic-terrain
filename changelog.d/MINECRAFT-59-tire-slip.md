bump: minor

### Added
- Public tire-slip input for vehicles: `TireSlip.report(level, contactBlock, slipSpeed, wheelLoadKg)`. A wheel reports how hard it is sliding and how much load it carries; erosion decides whether the surface changes, so burnouts, locked braking and drifting wear loose terrain while hard surfaces resist. Needs `erosionEnabled = true` (default off).
- Op-only debug command `/dtslip <x y z> <slipSpeed> <loadKg>`.
