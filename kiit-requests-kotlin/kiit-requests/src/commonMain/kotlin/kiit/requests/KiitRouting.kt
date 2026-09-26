package kiit.requests

/**
 * Kiit's own three-part `area.api.action` routing convention, kept separate from
 * [ServerRequest] so it's opt-in, not baked into every server request. See [KiitRequest], the
 * type that actually combines the two.
 */
interface KiitRouting {
    val parts: List<String>

    /** The top-most, first part of the route, e.g. given `app.users.activate`, `app`. */
    val area: String
        get() = parts.getOrElse(0) { "" }

    /** The second part of the route, e.g. given `app.users.activate`, `users`. */
    val api: String
        get() = parts.getOrElse(1) { "" }

    /** The third part of the route, e.g. given `app.users.activate`, `activate`. */
    val action: String
        get() = parts.getOrElse(2) { "" }

    /** The dot-joined route, e.g. `app.users.activate`. */
    val fullName: String
        get() {
            return if (api.isEmpty()) {
                area
            } else if (action.isEmpty()) {
                "$area.$api"
            } else {
                "$area.$api.$action"
            }
        }

    fun isAction(targetArea: String, targetApi: String, targetAction: String): Boolean {
        return area == targetArea && api == targetApi && action == targetAction
    }
}
