package potfur.whatisnext

import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AutoSubmittedOptionsChunkTest : ChunkTestCase() {

    @Test
    fun `it does not submit when there is previous value`() {
        val storage = Storage<Id, String, Exception>()
            .apply { store(flowId, "B") }

        val chunk = AutoSubmittedChunk(OptionsChunk(storage, listOf("A"))) { c, id, r ->
            c.spec(id, r).map { it.options.singleOrNull() }
        }

        val result = chunk.view(flowId, requester).orThrow()

        assertEquals("B", result)
    }

    @Test
    fun `it submits when there is no previous value`() {
        val chunk = AutoSubmittedChunk(OptionsChunk(Storage(), listOf("A"))) { c, id, r ->
            c.spec(id, r).map { it.options.singleOrNull() }
        }

        val result = chunk.view(flowId, requester).orThrow()

        assertEquals("A", result)
    }

    @Test
    fun `it does nothing when theres nothing to submit`() {
        val chunk = AutoSubmittedChunk(OptionsChunk(Storage(), listOf("A", "B"))) { c, id, r ->
            c.spec(id, r).map { it.options.singleOrNull() }
        }

        val result = chunk.view(flowId, requester).orThrow()

        assertNull(result)
    }

    @Test
    fun `it preserves chunk name`() {
        val chunk = OptionsChunk(Storage(), listOf("A", "B"))

        assertEquals(
            chunk.type.name,
            AutoSubmittedChunk(chunk){ c, id, r ->
                c.spec(id, r).map { it.options.singleOrNull() }
            }.type.name
        )
    }
}
