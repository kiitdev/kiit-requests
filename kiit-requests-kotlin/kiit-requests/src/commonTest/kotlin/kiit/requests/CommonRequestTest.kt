package kiit.requests

import kotlin.test.Test
import kotlin.test.assertEquals

class CommonRequestTest {
    @Test
    fun apiFactoryBuildsDataAndMetaFromMaps() {
        val request =
            CommonRequest.api(
                area = "app",
                name = "users",
                action = "create",
                verb = Verb.Create,
                meta = mapOf("token" to "abc"),
                data = mapOf("email" to "alice@example.com"),
            )

        assertEquals(Source.API, request.source)
        assertEquals(Verb.Create, request.verb)
        assertEquals("alice@example.com", request.data.getString("email"))
        assertEquals("abc", request.meta.getString("token"))
        assertEquals(mapOf("token" to "abc"), request.meta.toMap())
    }

    @Test
    fun apiFactoryEmptyAreaOmitsLeadingDot() {
        val request = CommonRequest.api("", "users", "create", Verb.Create)
        assertEquals("users.create", request.path)
    }

    @Test
    fun cliFactoryUsesCliSourceAndDefaultVersion() {
        val request = CommonRequest.cli("app", "users", "create", Verb.Create)
        assertEquals(Source.CLI, request.source)
        assertEquals(Version(api = "0"), request.version)
    }

    @Test
    fun pathFactorySplitsDotDelimitedPath() {
        val request = CommonRequest.path("app.users.create", Verb.Create)
        assertEquals(listOf("app", "users", "create"), request.parts)
        assertEquals(Source.CLI, request.source)
    }

    @Test
    fun argsAndParamsStartEmptyFromTheseFactories() {
        val request = CommonRequest.api("app", "users", "create", Verb.Create)
        assertEquals(0, request.args.size())
        assertEquals(0, request.params.size())
    }
}
