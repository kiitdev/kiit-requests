package kiit.requests

import kiit.call.Identity
import kiit.inputs.Inputs
import kiit.inputs.Meta
import kotlinx.datetime.Instant

/**
 * The inbound side of [Request]: a call being dispatched, used for HTTP, CLI, queue-based, and
 * (planned) MCP-based calls.
 *
 * A `ServerRequest` is only ever constructed for a route that has resolved to a full
 * `area`/`name`/`action`. If resolution fails, no `ServerRequest` is built. That's a
 * construction-time contract every host (Ktor, CLI, queue, ...) has to uphold; it isn't enforced
 * by the type itself.
 *
 * `data`, `args`, and `params` are three separate, symmetric [Inputs], each scoped to exactly
 * one source: `data` is body arguments, `args` is query-string arguments, `params` is
 * path-declared parameters (named by the host at construction time, using the matched action's
 * declared param names). `ServerRequest` doesn't merge them into one flat map; that's a
 * dispatcher-level concern, not something this type does.
 */
interface ServerRequest : Request {
    val path: String
    val parts: List<String>

    /** Body arguments (POST/PUT/PATCH-style requests). */
    val data: Inputs

    /**
     * Path parameters, keyed by name as declared on the matched action.
     * e.g. `/app/orders/invoice/2024/03` with params `["year", "month"]` declared ->
     * `params = {"year": "2024", "month": "03"}`. Empty if no action could be resolved.
     */
    val params: Inputs
    override val meta: Meta

    /**
     * The underlying transport object this request was built from. Shape depends on [source]:
     * the underlying `ApplicationCall`/`HttpServletRequest` for HTTP, a shell command structure
     * for CLI, a queue message for queue-based requests. Each host documents the concrete shape
     * it returns.
     */
    val raw: Any?

    /** Output format of the result, e.g. json by default, csv, props. */
    val output: String?

    /** Caller-supplied correlation labels, for tracking/grouping requests, may span several. */
    val tag: List<String>

    /**
     * Identifies the calling service/component/app. Strictly required, every caller (mobile,
     * web, CLI, service-to-service) must supply this. A [ServerRequest] cannot be constructed
     * without a valid [callerId], the same construction-time contract already applied to
     * [area]/[name]/[action].
     */
    val callerId: Identity
    val files: Files

    /** The full path of the route. */
    val fullName: String
        get() {
            return if (name.isEmpty()) {
                area
            } else if (action.isEmpty()) {
                "$area.$name"
            } else {
                "$area.$name.$action"
            }
        }

    /** The top-most, first part of the route, e.g. given `/app/users/activate`, `app`. */
    val area: String
        get() = parts.getOrElse(0) { "" }

    /** The second part of the route, e.g. given `/app/users/activate`, `users`. */
    val name: String
        get() = parts.getOrElse(1) { "" }

    /** The third part of the route, e.g. given `/app/users/activate`, `activate`. */
    val action: String
        get() = parts.getOrElse(2) { "" }

    fun isAction(targetArea: String, targetName: String, targetAction: String): Boolean {
        return area == targetArea && name == targetName && action == targetAction
    }

    /** Destructured into key/value pairs for structured logging. */
    fun structured(): List<Pair<String, Any?>> {
        return listOf(
            ServerRequest::area.name to area,
            ServerRequest::name.name to name,
            ServerRequest::action.name to action,
            ServerRequest::source.name to source.id,
            ServerRequest::verb.name to verb.name,
            ServerRequest::tag.name to tag,
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
        parts: List<String> = this.parts,
        source: Source = this.source,
        verb: Verb = this.verb,
        data: Inputs = this.data,
        args: Inputs = this.args,
        params: Inputs = this.params,
        meta: Meta = this.meta,
        raw: Any? = this.raw,
        output: String? = this.output,
        tag: List<String> = this.tag,
        version: Version = this.version,
        requestId: String = this.requestId,
        callerId: Identity = this.callerId,
        files: Files = this.files,
        trace: Trace? = this.trace,
        timestamp: Instant = this.timestamp,
    ): ServerRequest
}
