package kiit.requests

/**
 * A correlation label on a request, either a bare value or a key/value pair. [parse] splits a
 * raw string on its first `:` (e.g. `"retry"` -> [Basic], `"region:us-east-1"` -> [Keyed]).
 */
sealed class Tag {
    abstract val raw: String

    data class Keyed(val key: String, val value: String) : Tag() {
        override val raw: String = "$key:$value"
    }

    data class Basic(val value: String) : Tag() {
        override val raw: String get() = value
    }

    companion object {
        fun parse(raw: String): Tag {
            val idx = raw.indexOf(':')
            return if (idx < 0) Basic(raw) else Keyed(raw.substring(0, idx), raw.substring(idx + 1))
        }
    }
}
