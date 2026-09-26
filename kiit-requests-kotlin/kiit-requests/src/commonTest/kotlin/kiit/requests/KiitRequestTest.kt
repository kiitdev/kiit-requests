package kiit.requests

import kiit.call.Identity
import kotlin.test.Test
import kotlin.test.assertEquals

private val caller = Identity.test("kiit", "tests")

class KiitRequestTest {
    @Test
    fun apiFactoryBuildsDataAndMetaFromMaps() {
        val request =
            KiitRequest.api(
                area = "app",
                api = "users",
                action = "create",
                verb = Verb.Create,
                callerId = caller,
                meta = mapOf("token" to "abc"),
                data = mapOf("email" to "alice@example.com"),
            )

        assertEquals(Source.Api, request.source)
        assertEquals(Verb.Create, request.verb)
        assertEquals(caller, request.callerId)
        assertEquals("alice@example.com", request.data.getString("email"))
        assertEquals("abc", request.meta.getString("token"))
        assertEquals(mapOf("token" to "abc"), request.meta.toMap())
    }

    @Test
    fun apiFactoryEmptyAreaOmitsLeadingDot() {
        val request = KiitRequest.api("", "users", "create", Verb.Create, caller)
        assertEquals("users.create", request.path)
    }

    @Test
    fun cliFactoryUsesCliSourceAndDefaultVersion() {
        val request = KiitRequest.cli("app", "users", "create", Verb.Create, caller)
        assertEquals(Source.Cli, request.source)
        assertEquals(Version(api = "0"), request.version)
    }

    @Test
    fun pathFactorySplitsDotDelimitedPath() {
        val request = KiitRequest.path("app.users.create", Verb.Create, caller)
        assertEquals(listOf("app", "users", "create"), request.parts)
        assertEquals(Source.Cli, request.source)
    }

    @Test
    fun argsAndParamsStartEmptyFromTheseFactories() {
        val request = KiitRequest.api("app", "users", "create", Verb.Create, caller)
        assertEquals(0, request.args.size())
        assertEquals(0, request.params.size())
    }
}
