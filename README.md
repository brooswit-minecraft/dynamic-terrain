# dynamic-terrain

Dynamic terrain, erosion and driving-surface physics for Minecraft
(NeoForge 1.21.1, Java 21). Mod ID: `dynamicterrain`. MIT licensed.

**Current state:** consumes the extracted Layered Slopes block family (see
below) and implements `Smoothing.smooth(direction)` and
`Erosion.erode(amount)`, the material transition registry, the grading
tool, and cave-ins (epic MINECRAFT-58). Driving-surface physics is not yet
implemented.

## Architecture principle

**Systems emit generic physical or environmental operations; materials decide
how they respond.**

- Water does not know how dirt works; it calls erosion.
- A slipping tire does not know whether it is on sand or obsidian; it reports
  slip/erosion input.
- Vapor does not know which blocks have mossy variants; it applies a moisture
  transition, and unknown transitions are harmless no-ops.
- Structural stability checks support and breaks unsupported blocks without
  becoming part of erosion.
- Vehicle physics asks the contacted surface for driving properties rather
  than modifying terrain directly.

Design source: Confluence space BROOSWITMINECRAFT, "Consulting from an
Outsider — Dynamic Terrain, Erosion & Driving Surface Physics".

## Layered Slopes dependency (MINECRAFT-499)

The layered floor/ceiling block family (`LayeredBlock`/`LayeredGrassBlock`,
`LayerMath`, `Anchor`) moved to its own mod,
[Layered Slopes](https://github.com/brooswit-minecraft/layered-slopes), and
is now a **required** dependency (`neoforge.mods.toml`,
`compileOnly`+`runtimeOnly` on its release jar — see `build.gradle`'s
`downloadLayeredSlopes` task): this mod's smoothing, erosion, cave-ins and
grading-tool code consumes those classes via `LayeredMaterials`, but no
longer registers the blocks itself. Every layered block/item kept its
pre-split `dynamicterrain` registry name — Layered Slopes registers them
under that namespace specifically for this — so an existing save with
`dynamicterrain:layered_dirt` etc. placed keeps resolving once this mod is
upgraded past the split **with Layered Slopes installed alongside it**.
Grading tools, manual smoothing/terrain editing, world generation,
erosion/cave-ins and driving-surface physics all stay here; only the block
family itself moved.

**Required dependency, real release:** `gradle.properties` pins
`layeredslopes_version=0.2.0` and that release's actual SHA-512.
`build.gradle`'s `downloadLayeredSlopes` task fetches
`layeredslopes-0.2.0.jar` from
[Layered Slopes' GitHub release](https://github.com/brooswit-minecraft/layered-slopes/releases/tag/v0.2.0)
and verifies it against that pinned hash before every build, failing
loudly on a mismatch or on a missing/placeholder pin. The dependency is
one-way: install Layered Slopes 0.2.0+ alongside this mod, since
`neoforge.mods.toml` declares it as required and NeoForge refuses to load
this mod without it — but Layered Slopes itself runs fully standalone and
has no dependency on this mod (see its own README).

**Automated evidence (hosted CI, `ci.yml`):**
- `build`: compiles and tests against the real downloaded/verified Layered
  Slopes 0.2.0 jar, then runs `runGameTestServer` with both mods coinstalled
  (`CoinstalledGameTests`) — proving `LayeredMaterials`' vanilla-block
  lookups resolve to Layered Slopes' real registered blocks, and that a
  placed `dynamicterrain:layered_dirt` is still tagged for `CaveIns` with
  both mods actually loaded together, not just compiled together.
- `legacy-save-acceptance`: boots the actual pre-extraction build (this
  branch's merge-base with `main`) as a dedicated server, places
  representative layered blocks, saves the world to disk; then boots this
  mod's current head plus the real Layered Slopes release against that
  same saved world and reads the blocks back over RCON — proving a
  genuine legacy-world upgrade across the split, including a self-check
  that a deliberately wrong assertion is correctly rejected
  (`negative_case_verified`).
- `release-gate`: the usual changelog-fragment/version gate.

**Remaining limitation, not automated:** manual in-game playtest (actual
rendered frames, player-driven placement/collision feel) has not been
performed. Everything else MINECRAFT-499 required — standalone/coinstalled
load, client/server classloading and grass-tint rendering (see
Layered Slopes' own README), legacy registry/state resolution, and a real
save/reload upgrade across the split — has hosted, reproducible evidence
per the jobs above.

## Building

Requires a JDK 21 with `javac` on `JAVA_HOME`.

```sh
./gradlew build
```

The jar is written to `build/libs/dynamicterrain-<version>.jar`. The version
comes from `version=` in `gradle.properties`; override with
`-Pmod_version=<version>`.

## Contributing

Every PR that changes `src/` adds one `changelog.d/<TICKET>.md` fragment; see
`changelog.d/README.md`. Do not edit the version by hand.

## Releasing to Modrinth

The Modrinth project is created as a **draft** by the `Modrinth draft project create` workflow (manual dispatch only). Run it with `confirm` blank for a dry run that prints the payload; the live create needs the exact string `CREATE-DRAFT-PROJECT`, claims the slug permanently, and uses the org-level `MODRINTH_TOKEN`. Nothing here submits the project for Modrinth review; that is a deliberate human step. After a live create, record the printed project id as the repo variable `MODRINTH_PROJECT_ID`.
