package potfur.whatisnext.adapter

import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.value
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import potfur.whatisnext.ChunkAggregateWhatIsNext
import potfur.whatisnext.Id
import potfur.whatisnext.Specification
import potfur.whatisnext.Specification.State.COMPLETED
import potfur.whatisnext.Specification.State.REQUIRED
import potfur.whatisnext.Specification.Type
import potfur.whatisnext.StubChunk

class WhatIsNextTest : AdapterTestCase() {
    private val parent = StubChunk("parent", REQUIRED)
    private val child = StubChunk("child", COMPLETED)
    private val flow = ChunkAggregateWhatIsNext(parent, child)

    private val specLens = TestingJson.autoBody<DataEnvelope.Structured<List<Specification<out Type>>>>().toLens()
    private val statusLens = TestingJson.autoBody<DataEnvelope.Value<Boolean>>().toLens()
    private val genericLens = TestingJson.autoBody<DataEnvelope.Structured<List<GenericSpec>>>().toLens()

    private val adapter = WhatIsNextAdapter(
        basePath = basePath,
        flow = flow,
        specInjector = { r, v -> r.with(specLens of DataEnvelope.Structured(v)) },
        statusInjector = { r, v -> r.with(statusLens of DataEnvelope.Value(v)) },
        requesterResolver = requesterResolver
    ).asRoutingHttpAdapter()

    @Test
    fun `it returns flow specifications`() {
        val result = Request.next(flowId)

        assertEquals(
            DataEnvelope.Structured(
                listOf(
                    GenericSpec("parent", REQUIRED),
                    GenericSpec("child", COMPLETED)
                )
            ),
            result
        )
    }

    @Test
    fun `it returns flow completion status`() {
        val result = Request.status(flowId)

        assertEquals(DataEnvelope.Value(false), result)
    }

    private fun Request.Companion.next(flowId: Id) =
        Request(GET, "foo/{flowId}/next")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(adapter)
            .handleOrThrow(genericLens)

    private fun Request.Companion.status(flowId: Id) =
        Request(GET, "foo/{flowId}/status")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(adapter)
            .handleOrThrow(statusLens)
}
