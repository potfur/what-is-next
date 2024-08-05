package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peek
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import potfur.whatisnext.Option.Companion.A
import potfur.whatisnext.Option.Companion.B
import potfur.whatisnext.Specification.State.COMPLETED
import potfur.whatisnext.Specification.State.OPTIONAL
import potfur.whatisnext.Specification.State.REQUIRED

class WhatIsNextTest {
    private val flowId = Id.of(1)
    private val requester = Requester(2)

    @Test
    fun `it returns specs for all registered chunks`() {
        val parent = StubChunk("parent", REQUIRED)
        val child = StubChunk("child", COMPLETED)
        val flow = ChunkAggregateWhatIsNext(
            parent,
            child
        )

        assertEquals(
            listOf(
                parent.spec(flowId, requester).orThrow(),
                child.spec(flowId, requester).orThrow(),
            ),
            flow.whatIsNext(flowId, requester).orThrow()
        )
    }

    @Test
    fun `it allows for chunk to be dependant on other chunk`() {
        val parent = StubChunk("parent", REQUIRED)
        val child = StubChunk("child", COMPLETED)
        val flow = ChunkAggregateWhatIsNext(
            parent.then(child)
        )

        assertEquals(
            listOf(parent.type),
            flow.whatIsNext(flowId, requester).orThrow().map { it.type }
        )
    }

    @Test
    fun `it is completed when all chunks are completed`() {
        val parent = StubChunk("parent", COMPLETED)
        val child = StubChunk("child", COMPLETED)
        val flow = ChunkAggregateWhatIsNext(
            parent.then(child)
        )
        flow.whatIsNext(flowId, requester).orThrow()
        assertTrue(flow.isCompleted(flowId, requester).orThrow())
    }

    @Test
    fun `it is completed when all chunks are completed or optional`() {
        val parent = StubChunk("parent", COMPLETED)
        val child = StubChunk("child", OPTIONAL)
        val flow = ChunkAggregateWhatIsNext(
            parent.then(child)
        )
        flow.whatIsNext(flowId, requester).orThrow()
        assertTrue(flow.isCompleted(flowId, requester).orThrow())
    }

    @Test
    fun `it returns path for option`() {
        val options = OptionsChunk(Storage(), listOf(A, B))
        val fields = FieldsChunk(Storage())
        val info = ReadOnlyChunk { _, _ -> Success("DONE") }

        val flow = ChunkAggregateWhatIsNext(
            options.thenOnValue(
                branch(A, info),
                branch(B, fields.thenOnValue(branch("DUDE", info)) { it?.firstName }),
            ) { it }
        )

        assertEquals(
            listOf(options.type),
            flow.whatIsNext(flowId, requester).orThrow().map { it.type }
        )

        options.submit(flowId, requester, A).orThrowOnInvalid()

        assertEquals(
            listOf(options.type, info.type),
            flow.whatIsNext(flowId, requester).orThrow().map { it.type }
        )

        options.submit(flowId, requester, B).orThrowOnInvalid()

        assertEquals(
            listOf(options.type, fields.type),
            flow.whatIsNext(flowId, requester).orThrow().map { it.type }
        )

        fields.submit(flowId, requester, Fields("DUDE", "LEBOWSKI", "big@lebowski.dude")).orThrowOnInvalid()

        assertEquals(
            listOf(options.type, fields.type, info.type),
            flow.whatIsNext(flowId, requester).orThrow().map { it.type }
        )
    }

    private fun Result4k<List<Error>, Exception>.orThrowOnInvalid() =
        peek { if (it.isNotEmpty()) throw Exception(it.joinToString()) }
}
