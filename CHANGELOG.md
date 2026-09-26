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
  (`verb`/`version`/`meta`/`args`/`trace`/`requestId`/`timestamp`/`callerId`/`tags`). `callerId`
  is required here (not just a client-wide default), so "who made this call" is enforced for any
  `Request` implementation, not just kiit-rpc's own, and any policy/middleware wrapping a call
  sees it directly.
- New `ClientRequest : Request` interface: the outbound side, a flat `url` plus whatever a
  transport implementation (e.g. kiit-rpc's `RpcRequest`) adds on top.
- New `Tag` type (`Tag.Basic`/`Tag.Keyed`), replacing the old plain-`String` tags. `Tag.parse(raw)`
  splits on the first `:`.

### Changed
- `Request`/`CommonRequest` renamed to `ServerRequest`/`CommonServerRequest`, now extending the
  new base `Request`. `source` stays on `ServerRequest` (it's kiit-apis' own dispatch-trigger
  concept, not something a `ClientRequest` has); `callerId` and `tags`/`Tag` moved to the shared
  base.
- `ServerRequest.output: String?` renamed to `format: ContentType`, defaulting to
  `ContentTypes.Json` instead of `null`.
- `meta`/`args` are now `kiit.inputs.Meta`/`kiit.inputs.Args` (both `Inputs` + `Repeatable`),
  not plain `Inputs`. `Args` is `kiit-inputs`' new type, kept distinct from `Meta` even though
  they're structurally identical, so the two aren't interchangeable by accident.
