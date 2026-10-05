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
