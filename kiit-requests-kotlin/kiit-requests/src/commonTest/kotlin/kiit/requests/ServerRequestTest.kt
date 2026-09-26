package kiit.requests

import kiit.call.Identity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private val caller = Identity.test("kiit", "tests")

class ServerRequestTest {
    @Test
    fun cloneWithNoArgumentsReturnsAnEquivalentRequest() {
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        val cloned = request.clone()

        assertEquals(request.path, cloned.path)
        assertEquals(request.parts, cloned.parts)
        assertEquals(request.requestId, cloned.requestId)
        assertEquals(request.callerId, cloned.callerId)
    }

    @Test
    fun cloneOnlyChangesTheFieldsPassed() {
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        val cloned = request.clone(verb = Verb.Update, tags = listOf(Tag.Basic("retry")))

        assertEquals(Verb.Update, cloned.verb)
        assertEquals(listOf(Tag.Basic("retry")), cloned.tags)
        assertEquals(request.path, cloned.path)
        assertEquals(request.requestId, cloned.requestId)
    }

    @Test
    fun cloneCanChangeCallerId() {
        val other = Identity.test("kiit", "other-caller")
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        val cloned = request.clone(callerId = other)

        assertEquals(other, cloned.callerId)
        assertEquals(caller, request.callerId)
    }

    @Test
    fun everyRequestGetsAUniqueRequestId() {
        val first = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        val second = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        assertNotEquals(first.requestId, second.requestId)
    }

    @Test
    fun filesAndTraceDefaultToNoneAndNull() {
        val request = KiitRequest.api("app", "users", "activate", Verb.Get, caller)
        assertEquals(Files.None, request.files)
        assertEquals(null, request.trace)
    }
}
