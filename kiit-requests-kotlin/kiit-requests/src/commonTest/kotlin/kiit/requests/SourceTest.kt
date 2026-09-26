package kiit.requests

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SourceTest {
    @Test
    fun parsesKnownSources() {
        assertEquals(Source.Api, Source.parse("api"))
        assertEquals(Source.Cli, Source.parse("cli"))
        assertEquals(Source.Queue, Source.parse("queue"))
        assertEquals(Source.Job, Source.parse("job"))
        assertEquals(Source.Parent, Source.parse("@parent"))
        assertEquals(Source.All, Source.parse("*"))
    }

    @Test
    fun parseIsCaseInsensitive() {
        assertEquals(Source.Api, Source.parse("API"))
        assertEquals(Source.Cli, Source.parse("Cli"))
    }

    @Test
    fun unknownNameReturnsNull() {
        assertNull(Source.parse("webhook"))
    }

    @Test
    fun parentReferenceDetection() {
        assertTrue(Source.Parent.isParentReference())
        assertFalse(Source.Api.isParentReference())
    }

    @Test
    fun orElseSubstitutesOnlyForParentReference() {
        assertEquals(Source.Queue, Source.Parent.orElse(Source.Queue))
        assertEquals(Source.Api, Source.Api.orElse(Source.Queue))
    }
}
