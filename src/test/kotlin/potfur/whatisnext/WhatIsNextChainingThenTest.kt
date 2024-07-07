package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextChainingThenTest : ChunkTestCase() {
    @Test
    fun `it returns parent when predicate not met`() {
        val chunkA = StubChunk("parentA", Specification.State.REQUIRED)
        val chunkB = StubChunk("parentB", Specification.State.REQUIRED)
        val chunkC = StubChunk("child_A", Specification.State.COMPLETED)

        val result = chunkA.then(chunkB.then(chunkC))(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(chunkA.type), result)
    }

    @Test
    fun `it returns parent and first child when first predicate is met`() {
        val chunkA = StubChunk("parentA", Specification.State.COMPLETED)
        val chunkB = StubChunk("parentB", Specification.State.REQUIRED)
        val chunkC = StubChunk("child_A", Specification.State.COMPLETED)

        val result = chunkA.then(chunkB.then(chunkC))(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(chunkA.type, chunkB.type), result)
    }

    @Test
    fun `it returns parent and all children when all predicates are met`() {
        val chunkA = StubChunk("parentA", Specification.State.COMPLETED)
        val chunkB = StubChunk("parentB", Specification.State.COMPLETED)
        val chunkC = StubChunk("child_A", Specification.State.COMPLETED)

        val result = chunkA.then(chunkB.then(chunkC))(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(chunkA.type, chunkB.type, chunkC.type), result)
    }
}
