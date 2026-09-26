package kiit.requests

/**
 * The outbound side of [Request]: a call being made, not received, so it addresses a flat [url]
 * instead of the [ServerRequest] path/routing convention. Deliberately thin, everything specific
 * to a transport (a request body, auth, per-call timeout overrides) belongs on the concrete type
 * implementing this, e.g. kiit-rpc's `RpcRequest`, not here.
 */
interface ClientRequest : Request {
    val url: String
}
