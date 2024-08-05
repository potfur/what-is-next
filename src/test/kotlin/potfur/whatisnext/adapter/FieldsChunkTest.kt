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
import potfur.whatisnext.Fields
import potfur.whatisnext.FieldsChunk
import potfur.whatisnext.Id
import potfur.whatisnext.Storage

class FieldsChunkTest : AdapterTestCase() {
    private val storage = Storage<Id, Fields, Exception>()

    private val payloadLens = TestingJson.autoBody<DataEnvelope.Structured<Fields?>>().toLens()
    private val validationLens = TestingJson.autoBody<DataEnvelope.Structured<List<Error>>>().toLens()

    private val adapter = MutableChunkHttpAdapter(
        basePath = basePath,
        chunk = FieldsChunk(storage),
        injector = { r, v -> r.with(payloadLens of DataEnvelope.Structured(v)) },
        extractor = { r -> payloadLens(r).data },
        errors = { r, e -> r.with(validationLens of DataEnvelope.Structured(e)) },
        requesterResolver = requesterResolver
    ).asRoutingHttpAdapter()

    @Test
    fun `it serves empty data envelope for empty chunk`() {
        val result = Request.view(flowId)

        assertEquals(
            DataEnvelope.Structured<Fields?>(null),
            result
        )
    }

    @Test
    fun `it serves data envelope with current data`() {
        val value = Fields("Joe", "Doe", "j.d@com.com")
        storage.store(flowId, value).orThrow()

        val result = Request.view(flowId)

        assertEquals(
            DataEnvelope.Structured<Fields?>(value),
            result
        )
    }

    @Test
    fun `it returns list of validation errors when nothing sent`() {
        val result = Request.validate(flowId, null)

        assertEquals(
            DataEnvelope.Structured(listOf(Error("email", MISSING))),
            result
        )
    }


    @Test
    fun `it returns list of validation errors`() {
        val result = Request.validate(flowId, Fields(null, null, null))

        assertEquals(
            DataEnvelope.Structured(listOf(Error("email", MISSING))),
            result
        )
    }

    @Test
    fun `it returns empty list of validation errors when all is correct`() {
        val result = Request.validate(flowId, Fields("Joe", "Doe", "j.d@com.com"))

        assertEquals(
            DataEnvelope.Structured(emptyList<Error>()),
            result
        )
    }

    @Test
    fun `it returns list of validation errors if nothing submitted`() {
        val result = Request.submit(flowId, null)

        assertEquals(
            DataEnvelope.Structured(listOf(Error("email", MISSING))),
            result
        )
    }

    @Test
    fun `it returns list of validation errors if submitted value is invalid`() {
        val result = Request.submit(flowId, Fields(null, null, null))

        assertEquals(
            DataEnvelope.Structured(listOf(Error("email", MISSING))),
            result
        )
    }

    @Test
    fun `it returns empty list of validation errors when submit succeeded`() {
        val result = Request.submit(flowId, Fields("Joe", "Doe", "j.d@com.com"))

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
        Request(GET, "foo/{flowId}/field")
            .with(Path.value(Id).of("flowId") of flowId)
            .use(adapter)
            .handleOrThrow(payloadLens)

    private fun Request.Companion.validate(flowId: Id, data: Fields?) =
        Request(PUT, "foo/{flowId}/field/validate")
            .with(
                Path.value(Id).of("flowId") of flowId,
                payloadLens of DataEnvelope.Structured(data)
            )
            .use(adapter)
            .handleOrThrow(validationLens)

    private fun Request.Companion.submit(flowId: Id, data: Fields?) =
        Request(POST, "foo/{flowId}/field")
            .with(
                Path.value(Id).of("flowId") of flowId,
                payloadLens of DataEnvelope.Structured(data)
            )
            .use(adapter)
            .handleOrThrow(validationLens)

    private fun Request.Companion.delete(flowId: Id) =
        Request(DELETE, "foo/{flowId}/field")
            .with(
                Path.value(Id).of("flowId") of flowId,
            )
            .use(adapter)
            .handleOrThrow(validationLens)
}
