bump: minor

### Added
- Bounded weighted support search (internal building block for cave-ins, not yet wired to any block): a flood-fill through connected solid material scoring downward connections 3, sideways 2, upward 1, stopping as soon as a threshold is met, counting each cell once, and failing closed past a visited cap. Floating masses that are large enough hold themselves up by design.
