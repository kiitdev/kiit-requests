package kiit.requests

import kiit.call.Identity
import kiit.inputs.Args
import kiit.inputs.Inputs
import kiit.inputs.Meta
import kotlinx.datetime.Instant

/**
 * The inbound side of [Request]: a call being dispatched, used for HTTP, CLI, queue-based, and
 * (planned) MCP-based calls.
 *
 * Protocol-neutral, no routing convention baked in: [path] is just whatever raw path/route
 * string the call targeted. Kiit's own `area`/`api`/`action` three-part convention is opt-in,
 * see [KiitRouting]/[KiitRequest].
 *
 * `data`, `args`, and `params` are three separate, symmetric [Inputs], each scoped to exactly
 * one source: `data` is body arguments, `args` is query-string arguments, `params` is
 * path-declared parameters (named by the host at construction time, using the matched action's
 * declared param names). `ServerRequest` doesn't merge them into one flat map; that's a
 * dispatcher-level concern, not something this type does.
 */
interface ServerRequest : Request {
    val path: String
    val source: Source

    /** Body arguments (POST/PUT/PATCH-style requests). */
    val data: Inputs

    /**
     * Path parameters, keyed by name as declared on the matched action.
     * e.g. `/app/orders/invoice/2024/03` with params `["year", "month"]` declared ->
     * `params = {"year": "2024", "month": "03"}`. Empty if no action could be resolved.
     */
    val params: Inputs

    /**
     * The underlying transport object this request was built from. Shape depends on [source]:
     * the underlying `ApplicationCall`/`HttpServletRequest` for HTTP, a shell command structure
     * for CLI, a queue message for queue-based requests. Each host documents the concrete shape
     * it returns.
     */
    val raw: Any?

    /** Desired format of the result, e.g. JSON by default, CSV, props. */
    val format: ContentType
    val files: Files

    /** Destructured into key/value pairs for structured logging. */
    fun structured(): List<Pair<String, Any?>> {
        return listOf(
            ServerRequest::path.name to path,
            ServerRequest::source.name to source.id,
            ServerRequest::verb.name to verb.name,
            "tags" to tags.map { it.raw },
            ServerRequest::requestId.name to requestId,
            ServerRequest::callerId.name to callerId.id,
            ServerRequest::timestamp.name to timestamp.toString(),
        )
    }

    /**
     * Transforms/rewrites the request. Every parameter defaults to the current value, matching
     * Kotlin's `copy()` idiom, so a caller only names the fields it's actually changing, e.g.
     * `request.clone(meta = newMeta)`.
     */
    fun clone(
        path: String = this.path,
        source: Source = this.source,
        verb: Verb = this.verb,
        data: Inputs = this.data,
        args: Args = this.args,
        params: Inputs = this.params,
        meta: Meta = this.meta,
        raw: Any? = this.raw,
        format: ContentType = this.format,
        tags: List<Tag> = this.tags,
        version: Version = this.version,
        requestId: String = this.requestId,
        callerId: Identity = this.callerId,
        files: Files = this.files,
        trace: Trace? = this.trace,
        timestamp: Instant = this.timestamp,
    ): ServerRequest
}
