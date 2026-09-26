package kiit.requests

/**
 * What triggered kiit-apis to dispatch this request: `Api` (HTTP), `Queue`, `Job`, or `Cli`.
 * `Parent`/`All` aren't triggers, they're route-declaration values: an action can declare its
 * source as `@parent` (inherit its containing API's declared source) or `*` (accept any source).
 *
 * A closed set, deliberately: these are kiit-apis' own known dispatch/declaration values, not an
 * open, client-supplied taxonomy (that's [kiit.call.Agent]'s job, for the executable/service kind
 * behind an [kiit.call.Identity]).
 */
sealed class Source(val id: String) {
    object Parent : Source(PARENT)

    object All : Source(ALL)

    object Api : Source(API)

    object Queue : Source(QUEUE)

    object Job : Source(JOB)

    object Cli : Source(CLI)

    fun isParentReference(): Boolean = this.id == PARENT

    fun orElse(other: Source): Source = if (isParentReference()) other else this

    companion object {
        /** Reference to a parent value, e.g. if set on an action, refers to its parent API. */
        const val PARENT = "@parent"

        /** Enables all sources. */
        const val ALL = "*"

        /** HTTP for standard web/REST requests. */
        const val API = "api"

        /** Queue-based, requests saved and processed from a queue. */
        const val QUEUE = "queue"

        /** Job-triggered, e.g. a scheduler invoking an action on its own. */
        const val JOB = "job"

        /** Command line interactive. */
        const val CLI = "cli"

        fun parse(name: String): Source? {
            return when (name.lowercase()) {
                PARENT -> Parent
                ALL, "all" -> All
                API -> Api
                QUEUE -> Queue
                JOB -> Job
                CLI -> Cli
                else -> null
            }
        }
    }
}
