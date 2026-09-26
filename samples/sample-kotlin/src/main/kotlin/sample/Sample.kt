package sample

import kiit.call.Identity
import kiit.requests.KiitRequest
import kiit.requests.Tag
import kiit.requests.Verb

// The caller identity every request in this sample is attributed to. A real host would build
// this once at startup (Identity.api/cli/job/...) and reuse it across every request/job it
// handles, not construct a fresh one per call.
private val caller = Identity.api(company = "acme", area = "web", service = "gateway")

/**
 * Builds an API-style request the same way a Ktor/HTTP adapter would: area/api/action, a verb,
 * and body data.
 */
fun apiRequestExample() {
    val request = KiitRequest.api(
        area = "app",
        api = "users",
        action = "create",
        verb = Verb.Create,
        callerId = caller,
        meta = mapOf("authorization" to "Bearer abc123"),
        data = mapOf("email" to "alice@example.com"),
    )

    println("fullName=${request.fullName} verb=${request.verb}")
    println("email=${request.data.getString("email")}")
    println("auth=${request.meta.getString("authorization")}")
    println("caller=${request.callerId.full}")
    println("isAction=${request.isAction("app", "users", "create")}")
}

/**
 * `clone()` rewrites only the fields a caller names, everything else stays. Useful for policies/
 * middleware that need to transform a request without mutating it or rebuilding it from scratch.
 */
fun cloneExample() {
    val original = KiitRequest.cli("app", "jobs", "run", Verb.Execute, caller)
    val retried = original.clone(tags = original.tags + Tag.Basic("retry"))

    println("original tags=${original.tags}, retried tags=${retried.tags}")
    println("same requestId? ${original.requestId == retried.requestId}")
}

/** `structured()` gives every host consistent structured-logging fields for free. */
fun structuredLoggingExample() {
    val request = KiitRequest.path("app.orders.cancel", Verb.Delete, caller)
    request.structured().forEach { (key, value) -> println("$key=$value") }
}

fun main() {
    apiRequestExample()
    cloneExample()
    structuredLoggingExample()
}
