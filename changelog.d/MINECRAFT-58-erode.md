bump: minor

### Added
- `Erosion.erode(level, pos, amount)`: rolls against a resistance derived from hardness and blast resistance (log-compressed so obsidian-scale outliers stay bounded), then moves one layer downhill via smooth(), or falls back to the `damage` transition when material cannot move. Only layered blocks and full dirt/sand/gravel take part.
- Server config `erosionEnabled` (default **false**): erode() does nothing until it is switched on, so existing worlds are unaffected.
- Op-only debug command `/dterode <x y z> <amount>`.
