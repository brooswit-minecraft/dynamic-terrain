bump: patch

### Fixed
- Worldgen smoothing (MINECRAFT-64) now generates layered terraces on snowy slopes instead of skipping them. Previously any column with a thin vanilla snow layer on top was left as a full-block step, because the feature measured ground height through the snow and then required bare air above the ground to proceed. It now looks through the snow to the real ground underneath, and if that ground's height changes, carries the same snow depth along so it keeps sitting directly on the new surface instead of floating, leaving a gap, or disappearing. Snow-covered stone and snow-block terrain are both covered; columns with anything else above them (trees, plants, water, ...) are still left untouched.
