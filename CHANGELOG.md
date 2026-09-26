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
- New `KiitRouting` interface: kiit's own `area`/`api`/`action` three-part convention (`parts`,
  `fullName`, `isAction()`), kept separate from `ServerRequest` so it's opt-in rather than
  assumed on every inbound request.
- New `KiitRequest`: the real, ready-to-use type combining `ServerRequest` and `KiitRouting`,
  with the `api`/`cli`/`path` factory functions (moved from the old `CommonRequest`). What
  kiit-cli/kiit-tasks/kiit-apis actually dispatch on.

### Changed
- `Request` renamed to `ServerRequest`, now extending the new base `Request`. `source` stays on
  `ServerRequest` (it's kiit-apis' own dispatch-trigger concept, not something a `ClientRequest`
  has); `callerId` and `tags`/`Tag` moved to the shared base; `area`/`api`/`action`/`parts`/
  `isAction()`/`fullName` moved out entirely, to `KiitRouting`.
- `CommonRequest` renamed to `KiitRequest` (see Added), since its `api`/`cli`/`path` factories
  were always about building a kiit-routed request, not a generic one.
- The middle route segment renamed `name` → `api`, matching kiit-apis' own established
  convention (`area.api.action`), not a naming choice invented during this extraction.
- `ServerRequest.output: String?` renamed to `format: ContentType`, defaulting to
  `ContentTypes.Json` instead of `null`.
- `meta`/`args` are now `kiit.inputs.Meta`/`kiit.inputs.Args` (both `Inputs` + `Repeatable`),
  not plain `Inputs`. `Args` is `kiit-inputs`' new type, kept distinct from `Meta` even though
  they're structurally identical, so the two aren't interchangeable by accident. `KiitRequest`'s
  factories take `meta: Map<String, String>` now (was `Map<String, Any>`), matching `Meta`'s own
  always-a-string convention, rather than a bespoke pre-typed `Meta` implementation.
- `Response<T>.tag: List<String>` renamed to `tags: List<Tag>`, matching `Request`'s own move to
  `Tag`.

### Removed
- `Response<T>.desc: String?` — dead weight, never actually set by `Outcome<T>.toResponse()` (the
  one real construction path), and `Err.ErrorInfo.message` already covers per-instance failure
  detail.
