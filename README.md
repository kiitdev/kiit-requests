<div align="center">

# kiit-requests

**Protocol-neutral call modeling: a shared `Request` shape, an inbound `ServerRequest`, an outbound `ClientRequest`, `Response`, and `KiitRequest` for kiit's own routing convention. Kotlin Multiplatform.**

[![Build](https://img.shields.io/github/actions/workflow/status/kiitdev/kiit-requests/ci.yml?branch=main)](https://github.com/kiitdev/kiit-requests/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/kiitdev/kiit-requests)](./LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-purple.svg)](https://kotlinlang.org)

Part of [Kiit](https://www.kiit.dev)

</div>

## Table of Contents

- [Why](#why)
- [Start](#start)
- [Concepts](#concepts)
- [Usage](#usage)
- [Requirements](#requirements)
- [License](#license)

## Why

An HTTP request, a CLI invocation, and a queue message all carry the same underlying shape: a route, a verb, some input data, and a bit of metadata about the caller. Most code ends up modeling each one separately, so a handler written for HTTP can't be reused for CLI without a rewrite, and testing means standing up a real HTTP server just to exercise routing logic. kiit-requests is that one shape, so business logic can be written once against `ServerRequest` and wired up to whichever transport actually calls it.

`Request` is the smaller piece underneath: the fields genuinely common to *any* call, inbound or outbound (`verb`, `version`, `meta`, `args`, `trace`, `requestId`, `timestamp`, `callerId`, `tags`). `callerId` is required here, not optional, so "who made this call" is enforced for both directions, not just kiit-rpc's own convention. `ServerRequest` extends it with the inbound-only fields (`source`, `path`, `data`/`params`, `files`), and a `ClientRequest` extends it for the outbound side (an outbound RPC client, e.g. kiit-rpc, implements it with its own `url`/`auth`/`body`). One vocabulary either direction, no re-translation at the boundary.

`ServerRequest` itself carries no routing opinion, `path` is just whatever raw route string a call targeted. Kiit's own `area`/`api`/`action` three-part convention is opt-in, via `KiitRouting`, and `KiitRequest` is the real, ready-to-use type that combines the two, the one kiit-cli/kiit-tasks/kiit-apis actually dispatch on:

```kotlin
import kiit.call.Identity
import kiit.requests.KiitRequest
import kiit.requests.Verb

val caller = Identity.api(company = "acme", area = "web", service = "gateway")
val request = KiitRequest.api(
    area = "app",
    api = "users",
    action = "create",
    verb = Verb.Create,
    callerId = caller,
    data = mapOf("email" to "alice@example.com"),
)

println(request.fullName)              // app.users.create
println(request.data.getString("email"))
```

The three-part `area`/`api`/`action` route convention is structural, not just a naming habit. `fullName` gives a stable dot-joined identity for logging and dispatch lookup, and `isAction(area, api, action)` checks against it directly.

## Start

kiit-requests hasn't been published to Maven Central yet. Once it is:

```kotlin
dependencies {
    implementation("dev.kiit:kiit-requests:<version>")
}
```

`kiit-requests` depends on `dev.kiit:kiit-inputs` (for `Inputs`/`Meta`) and `dev.kiit:kiit-call`
(for `Identity`/`About`/`Agent`) transitively, you don't need to add either separately.

**Build a request and read typed values from it:**

```kotlin
import kiit.call.Identity
import kiit.requests.KiitRequest
import kiit.requests.Verb

val caller = Identity.job(company = "acme", area = "jobs", service = "scheduler")
val request = KiitRequest.cli(
    area = "app",
    api = "jobs",
    action = "run",
    verb = Verb.Execute,
    callerId = caller,
    data = mapOf("retries" to 3),
)

val retries = request.data.getInt("retries")
```

`data`/`params` are backed by kiit-inputs' `RecordMap`, which reads with a plain cast rather than parsing strings. The map passed in needs to already hold the right type per key (`3`, not `"3"`), the same way a JSON body's values are already typed once deserialized. A host parsing raw strings (CLI flags, query params) does that parsing itself before constructing the `ServerRequest`. `meta` is always `Map<String, String>`, matching `Meta`'s own wire-format-string convention. `args` starts empty from these factories; a host resolving query-string args populates it directly (via `clone`) rather than going through them.

**Rewrite a request without mutating it**, the pattern a policy or middleware layer needs:

```kotlin
import kiit.requests.Tag

val retried = request.clone(tags = request.tags + Tag.Basic("retry"))
```

`clone()` defaults every parameter to the current value, so a caller only names what it's actually changing.

**Turn a handler's `Outcome<T>` into a `Response<T>`**, ready for a responder to serialize:

```kotlin
import kiit.requests.toResponse

val response = someHandler(request).toResponse()
println("${response.success} ${response.status.name} ${response.value}")
```

See [`samples/sample-kotlin`](./samples/sample-kotlin) for a runnable end-to-end example.

## Concepts

| Term | What it is |
|---|---|
| **`Request`** | The purely common shape of a call, either direction: `verb`, `version`, `meta`, `args`, `trace`, `requestId`, `timestamp`, `callerId`, `tags`. |
| **`ServerRequest`** | The inbound side: `Request` plus `source`, `path`, `data`/`params`, `files`, `raw`, `format`. No routing convention baked in, `path` is just whatever raw route string the call targeted. |
| **`KiitRouting`** | Kiit's own `area`/`api`/`action` three-part convention (`parts`, `fullName`, `isAction()`), kept separate from `ServerRequest` so it's opt-in, not assumed on every inbound request. |
| **`KiitRequest`** | The real, ready-to-use type combining `ServerRequest` and `KiitRouting`, with `api`/`cli`/`path` factory functions. What kiit-cli/kiit-tasks/kiit-apis actually dispatch on. |
| **`ClientRequest`** | The outbound side: `Request` plus a flat `url`. Deliberately thin, an RPC client (e.g. kiit-rpc's `RpcRequest`) implements it and adds its own body/auth/options. |
| **`Source`** (`ServerRequest` only) | What triggered kiit-apis to dispatch a request: `Api`, `Queue`, `Job`, `Cli`. `Parent`/`All` are route-declaration values (inherit from a parent action, or accept any source), not dispatch triggers. A closed set. |
| **`Identity` / `callerId`** (from kiit-call) | `callerId: Identity` identifies the calling service/component. Required on `Request` itself, both directions, so a policy/middleware wrapping any call sees it directly, and any `ClientRequest` implementation is bound by it too, not just kiit-rpc's own convention. |
| **`Verb`** | A protocol-neutral CRUD-ish verb (`Get`, `Query`, `Create`, `Update`, `Upsert`, `Patch`, `Delete`, `Execute`), deliberately not HTTP-shaped. |
| **`data` / `params`** (`ServerRequest` only) | Two separate `Inputs` (from kiit-inputs): body arguments and path-declared parameters. `ServerRequest` keeps them apart rather than merging them into one flat map; that merge is a dispatcher concern. |
| **`args`** | Query/call arguments, as an `Args` (from kiit-inputs). Same shape as `Meta` (`Inputs` + `Repeatable`), kept as its own type so the two aren't interchangeable by accident. |
| **`meta`** | Header-like settings for the call (HTTP headers, CLI flags, queue attributes), as a `Meta`. Keys that legitimately repeat (e.g. `Set-Cookie`) are readable via `getAll(key)`. |
| **`Version`** | A call's API-level version, plus an optional action-level override. |
| **`Trace`** | Distributed tracing context (W3C Trace Context shape), carried through faithfully but never created or managed by kiit-requests itself. |
| **`Tag`** | A correlation label, either a bare value (`Tag.Basic`) or a key/value pair (`Tag.Keyed`). `Tag.parse(raw)` splits on the first `:`. |
| **`Files`** (`ServerRequest` only) | Lazy access to files attached to a request, e.g. a multipart upload. `Files.None` covers hosts with no file concept. |
| **`format`** (`ServerRequest` only) | Desired response format, a `ContentType`, defaulting to `ContentTypes.Json`. |
| **`Content` / `ContentType`** (`ContentText`/`ContentData`/`ContentFile`) | Typed byte content with a `ContentType` attached, for responses or file-like values that need to carry their format along with them. |
| **`Response<T>`** | The complement to `ServerRequest`: a handler's `Outcome<T>` flattened into `status`/`value`/`err`/`meta`/`tag`/`desc`, ready for a responder to serialize. `CommonResponse<T>` is the default implementation; `Outcome<T>.toResponse()` builds one. |

`clone()` rewrites a request without mutating it: every field defaults to `this.<field>`, matching Kotlin's own `copy()` idiom. `structured()` destructures a request into key/value pairs for structured logging, the same fields every host produces regardless of which protocol it's adapting.

## Usage

**Good fit if:**
1. You want to write handler/business logic once and reuse it across HTTP, CLI, and queue/job entry points, instead of one implementation per transport.
2. You're building a protocol adapter (an HTTP framework binding, a CLI parser, a queue consumer) and want a stable shape to construct and hand off.
3. You need to transform a request in flight, a policy layer stripping a header, a retry wrapper adding a tag, without mutating the original.
4. You're building an outbound client and want to share `Verb`/`Version`/`Trace`/`Content`/`callerId` with the inbound side, via the common `Request` base.

**Probably not necessary if:**
1. You only ever have one transport and don't expect a second one, a framework's own request type is simpler in that case.
2. You don't need a status taxonomy on responses, a plain return value/exception is enough.

## Requirements

- Kotlin Multiplatform
- JVM, Android, iOS (arm64, simulator arm64, x64)
- Depends on `dev.kiit:kiit-inputs`, `dev.kiit:kiit-call`, `dev.kiit:kiit-codes`, and `dev.kiit:kiit-result` (all transitively available to consumers via `api`)

## License

[Apache License 2.0](./LICENSE)

---

<div align="center">

**kiit-requests** is one module of [Kiit](https://www.kiit.dev), a lightweight, modular
Kotlin toolkit for building server applications, APIs, CLIs, and jobs.

**Adopt one module at a time.**

</div>
