package potfur.whatisnext.adapter

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.value
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import potfur.whatisnext.ForwardChunk
import potfur.whatisnext.Id
import potfur.whatisnext.Requester
import potfur.whatisnext.Specification
import potfur.whatisnext.Specification.Type
import potfur.whatisnext.WhatIsNext

class ForwardChunkTest : AdapterTestCase() {
    private val targetId = 1
    private val target = object : WhatIsNext<Id, Type, Specification<Type>, Requester, Exception> {
        override fun whatIsNext(flowId: Id, requester: Requester) = Failure(Exception())
        override fun isCompleted(flowId: Id, requester: Requester) = Success(true)
    }
    private val payloadLens = TestingJson.autoBody<DataEnvelope.Value<Int?>>().toLens()

    private val adapter = ChunkHttpAdapter(
        basePath = basePath,
        chunk = ForwardChunk(targetId, target),
        injector = { r, v -> r.with(payloadLens of DataEnvelope.Value(v)) },
        requesterResolver = requesterResolver
    ).asRoutingHttpAdapter()

    @Test
    fun `it serves forwarding data for chunk`() {
        val result = Request.view(flowId)

        assertEquals(
            DataEnvelope.Value<Int?>(targetId),
            result
        )
    }

    private fun Request.Companion.view(flowId: Id) =
        Request(GET, "foo/{flowId}/forward")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(adapter)
            .handleOrThrow(payloadLens)
}
