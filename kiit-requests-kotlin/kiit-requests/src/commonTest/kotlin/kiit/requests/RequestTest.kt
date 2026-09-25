package kiit.requests

import kiit.call.Identity
import kiit.call.Verb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private val caller = Identity.test("kiit", "tests")

class RequestTest {
    @Test
    fun fullNameJoinsWhicheverPartsArePresent() {
        val full = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        assertEquals("app.users.activate", full.fullName)

        val noAction = full.clone(parts = listOf("app", "users"))
        assertEquals("app.users", noAction.fullName)

        val areaOnly = full.clone(parts = listOf("app"))
        assertEquals("app", areaOnly.fullName)
    }

    @Test
    fun areaNameActionComeFromParts() {
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        assertEquals("app", request.area)
        assertEquals("users", request.name)
        assertEquals("activate", request.action)
    }

    @Test
    fun missingPartsDefaultToEmptyString() {
        val request = CommonRequest.api("", "", "", Verb.Get, caller)
        assertEquals("", request.area)
        assertEquals("", request.name)
        assertEquals("", request.action)
    }

    @Test
    fun isActionMatchesAllThreeParts() {
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        assertTrue(request.isAction("app", "users", "activate"))
        assertFalse(request.isAction("app", "users", "deactivate"))
    }

    @Test
    fun structuredIncludesKeyFieldsAsStrings() {
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        val structured = request.structured().toMap()

        assertEquals("app", structured["area"])
        assertEquals("users", structured["name"])
        assertEquals("activate", structured["action"])
        assertEquals("api", structured["source"])
        assertEquals("Get", structured["verb"])
        assertEquals(request.requestId, structured["requestId"])
        assertEquals(caller.id, structured["callerId"])
    }

    @Test
    fun cloneWithNoArgumentsReturnsAnEquivalentRequest() {
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        val cloned = request.clone()

        assertEquals(request.path, cloned.path)
        assertEquals(request.parts, cloned.parts)
        assertEquals(request.requestId, cloned.requestId)
        assertEquals(request.callerId, cloned.callerId)
    }

    @Test
    fun cloneOnlyChangesTheFieldsPassed() {
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        val cloned = request.clone(verb = Verb.Update, tag = listOf("retry"))

        assertEquals(Verb.Update, cloned.verb)
        assertEquals(listOf("retry"), cloned.tag)
        assertEquals(request.path, cloned.path)
        assertEquals(request.requestId, cloned.requestId)
    }

    @Test
    fun cloneCanChangeCallerId() {
        val other = Identity.test("kiit", "other-caller")
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        val cloned = request.clone(callerId = other)

        assertEquals(other, cloned.callerId)
        assertEquals(caller, request.callerId)
    }

    @Test
    fun everyRequestGetsAUniqueRequestId() {
        val first = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        val second = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        assertNotEquals(first.requestId, second.requestId)
    }

    @Test
    fun filesAndTraceDefaultToNoneAndNull() {
        val request = CommonRequest.api("app", "users", "activate", Verb.Get, caller)
        assertEquals(Files.None, request.files)
        assertEquals(null, request.trace)
    }
}
