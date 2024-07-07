package potfur.whatisnext

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import potfur.whatisnext.Specification.State.COMPLETED
import potfur.whatisnext.Specification.State.REQUIRED
import potfur.whatisnext.Specification.Type

class ForwardChunkTest : ChunkTestCase() {

    @Test
    fun `it has required specification when target next is incomplete`() {
        val result = ForwardChunk(0, TargetWhatsNext(false))
            .spec(flowId, requester)
            .orThrow()

        assertEquals(result, ForwardSpec(0, REQUIRED))
    }

    @Test
    fun `it has completed specification when target next is completed`() {
        val result = ForwardChunk(0, TargetWhatsNext(true))
            .spec(flowId, requester)
            .orThrow()

        assertEquals(result, ForwardSpec(0, COMPLETED))
    }

    @Test
    fun `it returns target next id`() {
        val result = ForwardChunk(0, TargetWhatsNext(true))
            .view(flowId, requester)
            .orThrow()

        assertEquals(result, 0)
    }

    private fun TargetWhatsNext(completed: Boolean) =
        object : WhatIsNext<Id, Type, Specification<Type>, Requester, Exception> {
            override fun whatIsNext(flowId: Id, requester: Requester) = Failure(Exception())
            override fun isCompleted(flowId: Id, requester: Requester) = Success(completed)
        }
}
