package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextBranchingTest : ChunkTestCase() {
    @Test
    fun `it returns parent when parent is not yet completed`() {
        val parent = StubChunk("parent", Specification.State.REQUIRED, "A")
        val branchA = StubChunk("branchA", Specification.State.REQUIRED)
        val branchB = StubChunk("branchB", Specification.State.REQUIRED)

        val result = parent.thenOnValue(
            branch("A", branchA),
            branch("B", branchB)
        ) { it }(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type), result)
    }

    @Test
    fun `it returns parent when parent is completed but matching value`() {
        val parent = StubChunk("parent", Specification.State.COMPLETED, "C")
        val branchA = StubChunk("branchA", Specification.State.REQUIRED)
        val branchB = StubChunk("branchB", Specification.State.REQUIRED)

        val result = parent.thenOnValue(
            branch("A", branchA),
            branch("B", branchB)
        ) { it }(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type), result)
    }

    @Test
    fun `it returns parent with branch when parent is completed and matching value`() {
        val parent = StubChunk("parent", Specification.State.COMPLETED, "B")
        val branchA1 = StubChunk("branchA1", Specification.State.COMPLETED)
        val branchA2 = StubChunk("branchA2", Specification.State.REQUIRED)
        val branchB1 = StubChunk("branchB1", Specification.State.COMPLETED)
        val branchB2 = StubChunk("branchB2", Specification.State.REQUIRED)

        val result = parent.thenOnValue(
            branch("A", branchA1, branchA2),
            branch("B", branchB1.then(branchB2))
        ) { it }(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type, branchB1.type, branchB2.type), result)
    }
}
