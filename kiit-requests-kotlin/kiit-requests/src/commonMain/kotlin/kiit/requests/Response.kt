package kiit.requests

import kiit.codes.Err
import kiit.codes.Status
import kiit.inputs.ListMap
import kiit.inputs.Meta
import kiit.inputs.MetaMap

/**
 * A protocol-agnostic response, the complement to [Request]. One concrete shape for a handler's
 * outcome, ready to hand to a responder (HTTP, CLI, queue) for serialization, rather than a
 * sealed success/failure split like [kiit.result.Result] - build one from an
 * [kiit.result.Outcome] via [toResponse].
 *
 * [status] carries the full taxonomy (name/group/origin/message); this type doesn't duplicate
 * any of that onto separate fields. [desc] is per-instance, runtime-constructed detail, distinct
 * from [Status.message] (which is a constant, never built from runtime data).
 */
interface Response<out T> {
    val status: Status
    val value: T?
    val err: Err?
    val meta: Meta
    val tag: List<String>
    val desc: String?

    val success: Boolean get() = status.success

    fun withMeta(meta: Meta): Response<T>
}

/**
 * Default implementation of [Response].
 */
data class CommonResponse<out T>(
    override val status: Status,
    override val value: T? = null,
    override val err: Err? = null,
    override val meta: Meta = MetaMap(ListMap()),
    override val tag: List<String> = listOf(),
    override val desc: String? = null,
) : Response<T> {
    override fun withMeta(meta: Meta): Response<T> = copy(meta = meta)
}

/**
 * Converts an [kiit.result.Outcome] to a [Response], the usual way one gets built: a handler
 * computes an `Outcome<T>`, then flattens it to the boundary shape a responder serializes.
 */
fun <T> kiit.result.Outcome<T>.toResponse(): Response<T> =
    when (this) {
        is kiit.result.Success -> CommonResponse(status = status, value = value)
        is kiit.result.Failure -> CommonResponse(status = status, err = error)
    }
