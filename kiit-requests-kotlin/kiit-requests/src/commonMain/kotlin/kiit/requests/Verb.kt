package kiit.requests

/**
 * Protocol-neutral CRUD-ish verb for a request, deliberately not HTTP-shaped. Per-protocol
 * rendering (e.g. `Verb.Get` -> HTTP `GET`) is a host/adapter concern, not this type's.
 */
enum class Verb {
    // Reads
    Get,

    /** Safe, read-only, but body-bearing. Maps to HTTP's QUERY method. */
    Query,

    // Data changes
    Create,

    /** Maps to HTTP `PUT`. */
    Update,

    /** Create-or-replace. Also maps to HTTP `PUT`, same as [Update]. */
    Upsert,
    Patch,
    Delete,

    // Execute - general purpose/all other

    /** Mixed/general-processing actions with no single clean CRUD verb, e.g. an `inc()`. */
    Execute,
}
