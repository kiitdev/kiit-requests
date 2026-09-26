package kiit.requests

import kiit.call.Identity
import kiit.inputs.Args
import kiit.inputs.Meta
import kotlinx.datetime.Instant

/**
 * The purely common shape of a call, either direction: an inbound one being dispatched
 * ([ServerRequest]) or an outbound one being made ([ClientRequest]).
 *
 * @property verb The action being performed.
 * @property version API-level version, plus an optional action-level override.
 * @property meta Header-like settings for the call.
 * @property args Query/call arguments.
 * @property trace Distributed tracing context, if the caller supplied one.
 * @property requestId Unique identifier for this call instance.
 * @property timestamp When this call was constructed.
 * @property callerId Identifies the calling service/component. Required, for both directions.
 * @property tags Correlation labels for this call.
 */
interface Request {
    val verb: Verb
    val version: Version
    val meta: Meta
    val args: Args
    val trace: Trace?
    val requestId: String
    val timestamp: Instant
    val callerId: Identity
    val tags: List<Tag>
}
