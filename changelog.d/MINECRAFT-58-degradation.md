bump: minor

### Added
- Data-driven `damage` transition (`data/dynamicterrain/dynamicterrain/transitions/damage.json`) with the first degradation chains: grass → dirt → gravel → air and smooth stone → cobblestone → gravel → air (plain stone also goes to cobblestone). erode() applies it when material cannot move, and any block that declares a `damage` result now takes part in erosion. Other mods can extend it with a datapack file. Still gated by `erosionEnabled` (default off).
