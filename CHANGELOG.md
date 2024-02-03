# Change Log
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed
- Add a new arity to `make-widget-async` to provide a different widget shape.

## [0.3.0] - 2024-02-03
### Changed
- Rewrote Changelog

### Added
- `danger-mouse.catch-errors` has tests for `partition-all`

### Fixed
- `danger-mouse.transducers#carry-errors-xf` has been updated to properly handle `partition-all`
- `danger-mouse.catch-errors` catches errors on the arity-1 reduction
- Output of doc generation now in correct file

## [0.2.0] - 2024-02-02
### Changed
- Updated `GroupedResults` to have the same keys as the `danger-mouse.catch-errors` namespace

### Added
- Macros for updating results / errors easily in `danger-mouse.threading`
- `danger-mouse.catch-errors#catch-errors->>` and `#transduce->>` for better threading flow
- `danger-mouse.transducers#contain-errors-xf` to provide a connection between error-catching
  and representing errors in danger-mouse format

## 0.1.0 - 2024-02-02
### Added
- First published release

[Unreleased]: https://github.com/nomicflux/danger-mouse/compare/0.3.0...HEAD
[0.3.0]: https://github.com/nomicflux/danger-mouse/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/nomicflux/danger-mouse/compare/0.1.0...0.2.0

