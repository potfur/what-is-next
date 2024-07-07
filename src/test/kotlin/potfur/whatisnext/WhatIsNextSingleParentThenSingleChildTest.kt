package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class WhatIsNextSingleParentThenSingleChildTest : ChunkTestCase() {
    @Test
    fun `it returns parent chunk when predicate is not met`() {
        val parent = StubChunk("parent", Specification.State.REQUIRED)
        val child = StubChunk("child", Specification.State.COMPLETED)

        val result = parent.then(child)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type), result)
    }

    @Test
    fun `it returns parent and child when predicate is met`() {
        val parent = StubChunk("parent", Specification.State.COMPLETED)
        val child = StubChunk("child", Specification.State.COMPLETED)

        val result = parent.then(child)(emptyList(), flowId, requester)
            .orThrow().map { it.type }

        Assertions.assertEquals(listOf(parent.type, child.type), result)
    }
}
