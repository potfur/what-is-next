package potfur.whatisnext.adapter

import dev.forkhandles.result4k.Success
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.value
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import potfur.whatisnext.Id
import potfur.whatisnext.ReadOnlyChunk

class ReadOnlyChunkTest : AdapterTestCase() {
    private val value = "something"
    private val payloadLens = TestingJson.autoBody<DataEnvelope.Value<String?>>().toLens()

    private val adapter = ReadOnlyChunk(viewFn = { _, _ -> Success(value) })
        .asHttpAdapter(
            basePath = basePath,
            requesterResolver = requesterResolver,
            dataInjector = { r, v -> r.with(payloadLens of DataEnvelope.Value(v)) }
        )
        .asRoutingHttpAdapter()

    @Test
    fun `it serves data for chunk`() {
        val result = Request.view(flowId)

        assertEquals(
            DataEnvelope.Value<String?>(value),
            result
        )
    }

    private fun Request.Companion.view(flowId: Id) =
        Request(GET, "foo/{flowId}/read-only")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(adapter)
            .handleOrThrow(payloadLens)
}
