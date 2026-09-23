package sample

import kiit.requests.CommonRequest
import kiit.requests.Verb

/**
 * Builds an API-style request the same way a Ktor/HTTP adapter would: area/name/action, a verb,
 * and body data.
 */
fun apiRequestExample() {
    val request = CommonRequest.api(
        area = "app",
        name = "users",
        action = "create",
        verb = Verb.Create,
        meta = mapOf("authorization" to "Bearer abc123"),
        data = mapOf("email" to "alice@example.com"),
    )

    println("fullName=${request.fullName} verb=${request.verb}")
    println("email=${request.data.getString("email")}")
    println("auth=${request.meta.getString("authorization")}")
    println("isAction=${request.isAction("app", "users", "create")}")
}

/**
 * `clone()` rewrites only the fields a caller names, everything else stays. Useful for policies/
 * middleware that need to transform a request without mutating it or rebuilding it from scratch.
 */
fun cloneExample() {
    val original = CommonRequest.cli("app", "jobs", "run", Verb.Execute)
    val retried = original.clone(tag = original.tag + "retry")

    println("original tag=${original.tag}, retried tag=${retried.tag}")
    println("same requestId? ${original.requestId == retried.requestId}")
}

/** `structured()` gives every host consistent structured-logging fields for free. */
fun structuredLoggingExample() {
    val request = CommonRequest.path("app.orders.cancel", Verb.Delete)
    request.structured().forEach { (key, value) -> println("$key=$value") }
}

fun main() {
    apiRequestExample()
    cloneExample()
    structuredLoggingExample()
}
