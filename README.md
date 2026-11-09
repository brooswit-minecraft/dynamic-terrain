# dynamic-terrain

Dynamic terrain, erosion and driving-surface physics for Minecraft
(NeoForge 1.21.1, Java 21). Mod ID: `dynamicterrain`. MIT licensed.

**Current state: scaffold only.** This repo ships an empty mod plus the
Gradle project, CI and release gate. Layered blocks, `smooth(direction)`, the
transition registry and `erode(amount)` come next (epic MINECRAFT-58).

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

**Known pending step:** `gradle.properties`' `layeredslopes_version`/
`layeredslopes_sha512` are still the MINECRAFT-499 placeholder
(`UNRELEASED`) as of this PR — brooswit-minecraft/layered-slopes#1 (the
real block-extraction release) has not been reviewed/merged/released yet;
only its pre-extraction `v0.1.0` scaffold release exists, which must **not**
be pinned here (it ships none of the moved classes). `downloadLayeredSlopes`
fails loudly rather than silently building against a wrong or missing jar
when that placeholder is still set.

**Paired-PR integration validation (temporary):** per review guidance, this
PR's hosted CI checks out brooswit-minecraft/layered-slopes at its own
`minecraft-499-extract-layered-blocks` branch, builds it, and builds this
mod against that real jar via `-PlayeredslopesLocalJar=<path>` (see
`ci.yml` and `build.gradle`) — proving this mod actually compiles against
the extracted classes now, without pinning to the unrelated `v0.1.0`
scaffold or waiting on a release. This is how the stale
`ClientEvents`/`DynamicTerrainMod.LAYERED_GRASS` reference the review
caught got caught: it no longer exists (the grass tint handler moved to
Layered Slopes). The `-PlayeredslopesLocalJar` override and its paired
checkout CI step are removed once layered-slopes#1 merges and releases and
the real version+sha512 pin above is filled in; `downloadLayeredSlopes`
becomes the normal build path again at that point.

Not covered by this PR's automation, and not manually playtested: actual
client/server mod-loader runtime (the paired CI step proves compile-time
classloading only), standalone/coinstalled load, placement/stacking/
drops/render/collision/pathfinding behavior in a running game, and
legacy-world/save upgrade. Per review guidance, no GameTest/runtime
harness is introduced here (neither this mod nor Layered Slopes has ever
had one); that remains a bounded follow-up.

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
