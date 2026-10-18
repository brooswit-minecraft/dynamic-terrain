bump: patch

### Fixed
- Erosion no longer leaves disturbed grass grassy, or lets eroded soil keep a receiving grass block grassy: a successful erosion move now settles grass-group material as dirt on both the depleted source and the receiving cell, preserving layer counts. Manual grading (the pickaxe tool and `/dtsmooth`) is unchanged — grass still keeps its grass top while graded.
