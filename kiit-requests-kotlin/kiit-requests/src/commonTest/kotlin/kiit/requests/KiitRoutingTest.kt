package kiit.requests

import kiit.call.Identity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private val caller = Identity.test("kiit", "tests")

class KiitRoutingTest {
    @Test
    fun fullNameJoinsWhicheverPartsArePresent() {
        val full = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        assertEquals("app.users.activate", full.fullName)

        val noAction = full.copy(parts = listOf("app", "users"))
        assertEquals("app.users", noAction.fullName)

        val areaOnly = full.copy(parts = listOf("app"))
        assertEquals("app", areaOnly.fullName)
    }

    @Test
    fun areaApiActionComeFromParts() {
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        assertEquals("app", request.area)
        assertEquals("users", request.api)
        assertEquals("activate", request.action)
    }

    @Test
    fun missingPartsDefaultToEmptyString() {
        val request = KiitRequest.api("", "", "", Verb.Get, caller)
        assertEquals("", request.area)
        assertEquals("", request.api)
        assertEquals("", request.action)
    }

    @Test
    fun isActionMatchesAllThreeParts() {
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        assertTrue(request.isAction("app", "users", "activate"))
        assertFalse(request.isAction("app", "users", "deactivate"))
    }

    @Test
    fun structuredAddsRoutingFieldsOnTopOfServerRequestS() {
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        val structured = request.structured().toMap()

        assertEquals("app", structured["area"])
        assertEquals("users", structured["api"])
        assertEquals("activate", structured["action"])
        assertEquals("api", structured["source"])
        assertEquals("Get", structured["verb"])
        assertEquals(request.requestId, structured["requestId"])
        assertEquals(caller.id, structured["callerId"])
    }
}
