package potfur.whatisnext.adapter

import dev.forkhandles.result4k.orThrow
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.value
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import potfur.whatisnext.Error
import potfur.whatisnext.Error.Type.MISSING
import potfur.whatisnext.Id
import potfur.whatisnext.Option
import potfur.whatisnext.Option.Companion.A
import potfur.whatisnext.Option.Companion.B
import potfur.whatisnext.OptionsChunk
import potfur.whatisnext.Storage

class OptionsChunkTest : AdapterTestCase() {
    private val storage = Storage<Id, Option, Exception>()

    private val payloadLens = TestingJson.autoBody<DataEnvelope.Value<Option?>>().toLens()
    private val validationLens = TestingJson.autoBody<DataEnvelope.Structured<List<Error>>>().toLens()

    private val adapter = OptionsChunk(storage, listOf(A, B))
        .asHttpAdapter(
            basePath = basePath,
            requesterResolver = requesterResolver,
            dataInjector = { r, v -> r.with(payloadLens of DataEnvelope.Value(v)) },
            errorsInjector = { r, e -> r.with(validationLens of DataEnvelope.Structured(e)) },
            dataExtractor = { r -> payloadLens(r).data.value }
        )
        .asRoutingHttpAdapter()

    @Test
    fun `it serves empty data envelope for empty chunk`() {
        val result = Request.view(flowId)

        assertEquals(
            DataEnvelope.Value<Option?>(null),
            result
        )
    }

    @Test
    fun `it serves data envelope with current data`() {
        val value = A
        storage.store(flowId, value).orThrow()

        val result = Request.view(flowId)

        assertEquals(
            DataEnvelope.Value<Option?>(value),
            result
        )
    }

    @Test
    fun `it returns list of validation errors`() {
        val result = Request.validate(flowId, null)

        assertEquals(
            DataEnvelope.Structured(listOf(Error("data", MISSING))),
            result
        )
    }

    @Test
    fun `it returns empty list of validation errors when all is correct`() {
        val result = Request.validate(flowId, A)

        assertEquals(
            DataEnvelope.Structured(emptyList<Error>()),
            result
        )
    }

    @Test
    fun `it returns list of validation errors if submitted value is invalid`() {
        val result = Request.submit(flowId, null)

        assertEquals(
            DataEnvelope.Structured(listOf(Error("data", MISSING))),
            result
        )
    }

    @Test
    fun `it returns empty list of validation errors when submit succeeded`() {
        val result = Request.submit(flowId, A)

        assertEquals(
            DataEnvelope.Structured(emptyList<Error>()),
            result
        )
    }

    @Test
    fun `it no validation errors when deleting current content`() {
        val result = Request.delete(flowId)

        assertEquals(
            DataEnvelope.Structured(emptyList<Error>()),
            result
        )
    }

    private fun Request.Companion.view(flowId: Id) =
        Request(GET, "foo/{flowId}/option")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(adapter)
            .handleOrThrow(payloadLens)

    private fun Request.Companion.validate(flowId: Id, data: Option?) =
        Request(PUT, "foo/{flowId}/option/validate")
            .with(
                Path.value(Id).of("flowId") of flowId,
                payloadLens of DataEnvelope.Value(data)
            )
            .use(adapter)
            .handleOrThrow(validationLens)

    private fun Request.Companion.submit(flowId: Id, data: Option?) =
        Request(POST, "foo/{flowId}/option")
            .with(
                Path.value(Id).of("flowId") of flowId,
                payloadLens of DataEnvelope.Value(data)
            )
            .use(adapter)
            .handleOrThrow(validationLens)

    private fun Request.Companion.delete(flowId: Id) =
        Request(DELETE, "foo/{flowId}/option")
            .with(
                Path.value(Id).of("flowId") of flowId,
            )
            .use(adapter)
            .handleOrThrow(validationLens)
}
