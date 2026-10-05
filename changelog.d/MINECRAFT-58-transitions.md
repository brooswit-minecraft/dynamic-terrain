bump: minor

### Added
- Generic transition registry: `Transitions.applyTransition(level, pos, "moisture")`. What each block becomes is declared in datapack files `data/<namespace>/dynamicterrain/transitions/<name>.json` (block id to result block id), merged across namespaces, so other mods opt in without code. Unknown transitions and blocks are no-ops, and block state properties carry over where the result has them.
- Ships a `moisture` transition (cobblestone and stone bricks to their mossy forms) as the first example.
- Op-only debug command `/dttransition <x y z> <name>`.
