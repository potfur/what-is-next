package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextMultipleParentsThenMultipleChildrenTest : ChunkTestCase() {
    @Test
    fun `it returns all parents chunk when predicate is not met at all`() {
        val parentA = StubChunk("parentA", Specification.State.REQUIRED)
        val parentB = StubChunk("parentB", Specification.State.REQUIRED)
        val childA = StubChunk("child_A", Specification.State.COMPLETED)
        val childB = StubChunk("child_B", Specification.State.COMPLETED)

        val result = listOf(parentA, parentB).then(childA, childB)(emptyList(), flowId, requester)
            .orThrow().map { it.type }


        Assertions.assertEquals(listOf(parentA.type, parentB.type), result)
    }

    @Test
    fun `it returns all parents chunk when predicate is not met by one of parents`() {
        val parentA = StubChunk("parentA", Specification.State.REQUIRED)
        val parentB = StubChunk("parentB", Specification.State.COMPLETED)
        val childA = StubChunk("child_A", Specification.State.COMPLETED)
        val childB = StubChunk("child_B", Specification.State.COMPLETED)

        val result = listOf(parentA, parentB).then(childA, childB)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parentA.type, parentB.type), result)
    }

    @Test
    fun `it returns all parents and all children when predicate is met by all parents`() {
        val parentA = StubChunk("parentA", Specification.State.COMPLETED)
        val parentB = StubChunk("parentB", Specification.State.COMPLETED)
        val childA = StubChunk("child_A", Specification.State.COMPLETED)
        val childB = StubChunk("child_B", Specification.State.COMPLETED)

        val result = listOf(parentA, parentB).then(childA, childB)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parentA.type, parentB.type, childA.type, childB.type), result)
    }
}
