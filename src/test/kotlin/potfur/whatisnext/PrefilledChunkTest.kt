package potfur.whatisnext

import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class PrefilledChunkTest: ChunkTestCase() {

    @Test
    fun `it returns original value when present`() {
        val fields = Fields("Joe", "Doe", "j.d@com.com")
        val prefilled = Fields("James", "Blond", "j.b@com.com")

        val chunk = FieldsChunk(Storage())
        chunk.submit(flowId, requester, fields)

        val result = PrefilledChunk(chunk) { _, _ -> Success(prefilled) }
            .view(flowId, requester).orThrow()

        assertEquals(fields, result)
    }

    @Test
    fun `it returns prefilled value when view returns empty`() {
        val prefilled = Fields("James", "Blond", "j.b@com.com")

        val result = PrefilledChunk(FieldsChunk(Storage())) { _, _ -> Success(prefilled) }
            .view(flowId, requester).orThrow()

        assertEquals(prefilled, result)
    }

    @Test
    fun `it returns empty when no value and no prefill`() {
        val result = PrefilledChunk(FieldsChunk(Storage())) { _, _ -> Success(null) }
            .view(flowId, requester).orThrow()

        assertEquals(null, result)
    }
}
