package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextMultipleParentsThenSingleChildTest : ChunkTestCase() {
    @Test
    fun `it returns all parents chunk when predicate is not met at all`() {
        val parentA = StubChunk("parentA", Specification.State.REQUIRED)
        val parentB = StubChunk("parentB", Specification.State.REQUIRED)
        val child = StubChunk("child", Specification.State.COMPLETED)

        val result = listOf(parentA, parentB).then(child)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parentA.type, parentB.type), result)
    }

    @Test
    fun `it returns all parents chunk when predicate is not met by one of parents`() {
        val parentA = StubChunk("parentA", Specification.State.REQUIRED)
        val parentB = StubChunk("parentB", Specification.State.COMPLETED)
        val child = StubChunk("child", Specification.State.COMPLETED)

        val result = listOf(parentA, parentB).then(child)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parentA.type, parentB.type), result)
    }

    @Test
    fun `it returns all parents and all children when predicate is met by all parents`() {
        val parentA = StubChunk("parentA", Specification.State.COMPLETED)
        val parentB = StubChunk("parentB", Specification.State.COMPLETED)
        val child = StubChunk("child", Specification.State.COMPLETED)

        val result = listOf(parentA, parentB).then(child)(emptyList(), flowId, requester)
            .orThrow().map { it.type }


        Assertions.assertEquals(listOf(parentA.type, parentB.type, child.type), result)
    }
}
