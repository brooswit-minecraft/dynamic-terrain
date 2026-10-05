bump: minor

### Added
- Layered **Stone, Cobblestone, Smooth Stone, Deepslate and Cobbled Deepslate** (floor- and ceiling-anchored, 1 to 16 layers, pickaxe-mineable, in the Natural Blocks creative tab). Grading a full block of any of them with a pickaxe works like dirt: a stone block becomes 15 layers and the neighbour gains one, rejected exactly when smooth() would reject. They take part in cave-ins (`dynamicterrain:supported` tag, smooth stone added).
