# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

Types of changes:
- Added for new features.
- Changed for changes in existing functionality.
- Deprecated for soon-to-be removed features.
- Removed for now removed features.
- Fixed for any bug fixes.
- Security in case of vulnerabilities.

## [Unreleased]

## [0.2.0] - 2024-07-08

### Added

- Generated code is backtranslated so that quote shows as ' and so on.

### Fixed

- Multiline comments are now handled properly, so they'll show on multiple lines in the generated source. [#1](https://github.com/xadecimal/expose-api/issues/1)

## [0.1.0] - 2024-07-07

### Added

- Initial release
- expose-api function can be used to generate a public API namespace from impl vars.
- Doc and arities are copied over from the impl defn/defmacro.
- Support both defn and defnmacro.
- Support & rest var-args.
- Support multi-arity.
- Generated source file includes warning comment block to warn not to hand modify file.

[unreleased]: https://github.com/xadecimal/expose-api/compare/0.2.0...HEAD
[0.2.0]: https://github.com/xadecimal/expose-api/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/xadecimal/expose-api/tree/0.1.0
