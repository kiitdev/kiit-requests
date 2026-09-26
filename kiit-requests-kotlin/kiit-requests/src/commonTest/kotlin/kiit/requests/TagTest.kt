package kiit.requests

import kotlin.test.Test
import kotlin.test.assertEquals

class TagTest {
    @Test
    fun parseWithNoColonBecomesBasic() {
        assertEquals(Tag.Basic("retry"), Tag.parse("retry"))
    }

    @Test
    fun parseWithColonBecomesKeyed() {
        assertEquals(Tag.Keyed("region", "us-east-1"), Tag.parse("region:us-east-1"))
    }

    @Test
    fun parseSplitsOnlyOnFirstColon() {
        assertEquals(Tag.Keyed("time", "10:30:00"), Tag.parse("time:10:30:00"))
    }

    @Test
    fun rawReconstructsTheOriginalString() {
        assertEquals("retry", Tag.Basic("retry").raw)
        assertEquals("region:us-east-1", Tag.Keyed("region", "us-east-1").raw)
    }
}
