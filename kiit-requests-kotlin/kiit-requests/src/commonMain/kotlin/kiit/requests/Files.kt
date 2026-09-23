package kiit.requests

/**
 * Access to files attached to a request, e.g. multipart uploads on the server side or
 * attachments staged for an outbound call on the client side. Always present and non-null on a
 * [Request]; hosts with no file concept (CLI, queue) supply [None].
 *
 * Read-only and lazy by design. Nothing is read until [get] is called. That matters
 * specifically on the server side, where multipart parsing is often sequential and one-shot,
 * so eagerly extracting every file at construction time isn't viable.
 */
interface Files {
    /** File names present, without reading their contents. */
    suspend fun names(): List<String>

    /** Lazily reads a named file's contents, if present. */
    suspend fun get(name: String): ContentFile?

    companion object {
        /** For hosts with no file concept, e.g. CLI, queue. */
        val None: Files =
            object : Files {
                override suspend fun names(): List<String> = emptyList()

                override suspend fun get(name: String): ContentFile? = null
            }
    }
}
