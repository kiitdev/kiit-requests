package kiit.requests

import kotlin.test.Test
import kotlin.test.assertEquals

class VerbTest {
    @Test
    fun coversEveryProtocolNeutralVerb() {
        assertEquals(
            listOf("Get", "Query", "Create", "Update", "Upsert", "Patch", "Delete", "Execute"),
            Verb.entries.map { it.name },
        )
    }
}
