package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextSingleParentThenMultipleChildrenTest : ChunkTestCase() {
    @Test
    fun `it returns parent chunk when predicate is not met`() {
        val parent = StubChunk("parent", Specification.State.REQUIRED)
        val childA = StubChunk("child_A", Specification.State.COMPLETED)
        val childB = StubChunk("child_B", Specification.State.COMPLETED)

        val result = parent.then(childA, childB)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type), result)
    }

    @Test
    fun `it returns parent and all children when predicate is met`() {
        val parent = StubChunk("parent", Specification.State.COMPLETED)
        val childA = StubChunk("child_A", Specification.State.COMPLETED)
        val childB = StubChunk("child_B", Specification.State.COMPLETED)

        val result = parent.then(childA, childB)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type, childA.type, childB.type), result)
    }
}
