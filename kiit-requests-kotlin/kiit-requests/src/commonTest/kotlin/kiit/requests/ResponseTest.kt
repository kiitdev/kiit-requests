package kiit.requests

import kiit.codes.Err
import kiit.codes.Succeeded
import kiit.codes.Unserved
import kiit.inputs.ListMap
import kiit.inputs.MetaMap
import kiit.result.Failure
import kiit.result.Success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResponseTest {
    @Test
    fun successOutcomeConvertsToResponseWithValueAndNoErr() {
        val response = Success("alice", Succeeded.CREATED).toResponse()

        assertTrue(response.success)
        assertEquals(Succeeded.CREATED, response.status)
        assertEquals("alice", response.value)
        assertNull(response.err)
    }

    @Test
    fun failureOutcomeConvertsToResponseWithErrAndNoValue() {
        val err = Err.of("boom")
        val response = Failure(err, Unserved.TIMEOUT).toResponse()

        assertEquals(false, response.success)
        assertEquals(Unserved.TIMEOUT, response.status)
        assertNull(response.value)
        assertEquals(err, response.err)
    }

    @Test
    fun withMetaReplacesMetaWithoutChangingOtherFields() {
        val response = CommonResponse(status = Succeeded.SUCCESS, value = 42)
        val meta = MetaMap(ListMap(listOf("x-request-id" to "abc")))

        val updated = response.withMeta(meta)

        assertEquals(meta, updated.meta)
        assertEquals(42, updated.value)
        assertEquals(Succeeded.SUCCESS, updated.status)
    }

    @Test
    fun descIsIndependentOfStatusMessage() {
        val response = CommonResponse<Unit>(status = Unserved.RATE_LIMITED, desc = "retry after 30s")

        assertEquals("retry after 30s", response.desc)
        assertEquals("Too many requests; try again later.", response.status.message)
    }
}
