# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [21.1.8]

### Changed
- All Unearther blocks are now in the `c:relocation_not_supported` block tag
- Worker villagers may no longer be converted to witches with a lightning strike
- Updated KubeJS support to KubeJS 7.2 
  - **IMPORTANT**: this release will not work with earlier versions of KubeJS!

## [21.1.7]

### Fixed
- Added some villager entity sanity checking to ensure only the correct entity is present within the Unearther
  - Should normally always be the case, but there are reports of duplicating villagers, cause unknown...
- Hopefully fixed issues some players encountered with worker entities duplicating themselves
- Fixed Ultimine brushing affecting blocks it shouldn't in shaped ultimine modes

## [21.1.6]

### Fixed
- Fixed Unreakable Brush not working for manual brushing of blocks

## [21.1.5]

### Added
- Added server config `encoded_villager_type` to force village type override for encoded villagers
  - Default `ftb:stone`; if empty string or invalid, the villager's current type is used (error logged if invalid)

## [21.1.4]

### Changed
- JEI recipe display now has room for 12 output slots

### Fixed
- Fixed behaviour with ultimining and brushes with low durability

## [21.1.3]

### Fixed
- Fixed cross-mod compat issue where "Obstructed by block!" messages could be spuriously displayed

## [21.1.2]

### Fixed
- Fixed background colour of Unearther GUI progress arrow to match the overall GUI background

## [21.1.1]

### Added
- Initial release

## [21.1.0]

### Added

- The mod
