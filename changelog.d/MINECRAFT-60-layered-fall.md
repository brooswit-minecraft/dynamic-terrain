bump: minor

### Changed
- Cave-ins (`caveInsEnabled`): an unsupported `LayeredBlock` now falls under gravity exactly like vanilla sand/gravel via `FallingBlockEntity.fall`, instead of breaking and dropping its item. Material identity, layer height and anchor (floor/ceiling) are carried through the fall and the landing because the entity carries the full block state. Non-layered supported blocks (stone, dirt, gravel, etc.) are unchanged: they still break and drop.
