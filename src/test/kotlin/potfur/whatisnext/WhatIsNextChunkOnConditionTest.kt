package potfur.whatisnext

import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextChunkOnConditionTest : ChunkTestCase() {
    @Test
    fun `it returns chunk when predicate is met`() {
        val chunk = StubChunk("chunk", Specification.State.COMPLETED)

        val result = chunk.on { _, _ -> Success(true) }(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(chunk.type), result)
    }

    @Test
    fun `it does not return chunk when predicate is not met`() {
        val chunk = StubChunk("chunk", Specification.State.COMPLETED)

        val result = chunk.on { _, _ -> Success(false) }(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(emptyList<Specification.Type>(), result)
    }
}
