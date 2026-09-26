# Changelog

All notable changes to kiit-requests are documented here. Format follows
[Keep a Changelog](https://keepachangelog.com/), versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added
- Extracted from the Kiit monorepo as its own standalone module.
- `Verb`/`Version`/`Trace`/`Source`/`Content`/`ContentType` moved in from `kiit-call`, so
  kiit-requests now owns the whole call-shape domain, both directions.
- New base `Request` interface: the fields purely common to any call, inbound or outbound
  (`source`/`verb`/`version`/`meta`/`args`/`trace`/`requestId`/`timestamp`).
- New `ClientRequest : Request` interface: the outbound side, a flat `url` plus whatever a
  transport implementation (e.g. kiit-rpc's `RpcRequest`) adds on top.

### Changed
- `Request`/`CommonRequest` renamed to `ServerRequest`/`CommonServerRequest`, now extending the
  new base `Request`. Fields/behavior unchanged otherwise.
