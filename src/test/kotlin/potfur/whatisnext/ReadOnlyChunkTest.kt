package potfur.whatisnext

import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import potfur.whatisnext.Specification.State.COMPLETED


class ReadOnlyChunkTest: ChunkTestCase() {
    private val value = "something"

    private val chunk = ReadOnlyChunk(
        viewFn = { _, _ -> Success(value) }
    )

    @Test
    fun `it has completed specification`() {
        val result = chunk.spec(flowId, requester).orThrow()

        assertEquals(ReadOnlySpec, result)
        assertEquals(COMPLETED, result.state)
    }

    @Test
    fun `it returns defined value`() {
        val result = chunk.view(flowId, requester).orThrow()

        assertEquals(value, result)
    }
}
