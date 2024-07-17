package potfur.whatisnext

import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test


class AutoSubmittedFieldsChunkTest : ChunkTestCase() {
    private val fields = Fields("Joe", "Doe", "j.d@com.com")

    @Test
    fun `it does not submit when there is previous value`() {
        val previous = Fields("James", "Blond", "j.b@com.com")
        val storage = Storage<Id, Fields, Exception>()
            .apply { store(flowId, previous) }

        val chunk = AutoSubmittedChunk(FieldsChunk(storage)) { _, _, _ ->
            Success(fields)
        }

        val result = chunk.view(flowId, requester).orThrow()

        assertEquals(previous, result)
    }

    @Test
    fun `it submits when there is no previous value`() {
        val chunk = AutoSubmittedChunk(FieldsChunk(Storage())) { _, _, _ ->
            Success(fields)
        }

        val result = chunk.view(flowId, requester).orThrow()

        assertEquals(fields, result)
    }

    @Test
    fun `it does nothing when theres nothing to submit`() {
        val chunk = AutoSubmittedChunk(FieldsChunk(Storage())) { _, _, _ ->
            Success(null)
        }

        val result = chunk.view(flowId, requester).orThrow()

        assertNull(result)
    }

    @Test
    fun `it preserves chunk name`() {
        val chunk = FieldsChunk(Storage())

        assertEquals(
            chunk.type.name,
            AutoSubmittedChunk(chunk) { _, _, _ -> Success(fields) }.type.name
        )
    }
}
