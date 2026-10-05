bump: minor

### Added
- V1 cave-ins (needs `caveInsEnabled = true`, **default off**, separate from erosion): after a neighbouring block changes, blocks in the `dynamicterrain:supported` tag (dirt, sand, gravel, stone, deepslate, cobblestone, mud, clay and the layered blocks) run the bounded support search. If the connected material can't meet the block's required score, the block breaks and drops its item, and that update makes its neighbours recheck, so cave-ins cascade. Large floating masses hold themselves up. Unloaded chunks and bedrock count as support. Add blocks to the `dynamicterrain:supported` tag from a datapack to opt in.
- Server config `caveInsEnabled`.
