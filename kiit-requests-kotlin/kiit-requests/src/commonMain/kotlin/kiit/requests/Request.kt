package kiit.requests

import kiit.inputs.Inputs
import kotlinx.datetime.Instant

/**
 * The purely common shape of a call, either direction: an inbound one being dispatched
 * ([ServerRequest]) or an outbound one being made ([ClientRequest]). Anything that only makes
 * sense on one side (routing, `callerId`, `url`, `auth`) lives on that side, not here.
 */
interface Request {
    val source: Source
    val verb: Verb
    val version: Version
    val meta: Inputs
    val args: Inputs
    val trace: Trace?
    val requestId: String
    val timestamp: Instant
}
