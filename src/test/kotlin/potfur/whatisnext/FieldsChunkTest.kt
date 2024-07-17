package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import potfur.whatisnext.Error.Type.MISSING
import potfur.whatisnext.Specification.State.REQUIRED


class FieldsChunkTest : ChunkTestCase() {
    private val fields = Fields("Joe", "Doe", "j.d@com.com")

    @Test
    fun `it shows options in specification`() {
        val result = FieldsChunk(Storage())
            .spec(flowId, requester)
            .orThrow()

        assertEquals(FieldsSpec(REQUIRED, FieldsRequirements), result)
    }

    @Test
    fun `it submitted defined value`() {
        val chunk = FieldsChunk(Storage())
        assertNull(chunk.view(flowId, requester).orThrow())

        chunk.submit(flowId, requester, fields)
        assertEquals(fields, chunk.view(flowId, requester).orThrow())
    }

    @Test
    fun `it validates as missing for required field`() {
        val result = FieldsChunk(Storage())
            .validate(flowId, requester, null)
            .orThrow()

        assertEquals(listOf(Error("email", MISSING)), result)
    }

    @Test
    fun `it validates with no errors when option is valid`() {
        val result = FieldsChunk(Storage())
            .validate(flowId, requester, fields)
            .orThrow()

        assertEquals(emptyList<Error>(), result)
    }

    @Test
    fun `it rejects submit when required field is missing`() {
        val result = FieldsChunk(Storage())
            .submit(flowId, requester, null)
            .orThrow()

        assertEquals(listOf(Error("email", MISSING)), result)
    }

    @Test
    fun `it submits with no errors when option is valid`() {
        val chunk = FieldsChunk(Storage())
        val result = chunk
            .submit(flowId, requester, fields)
            .orThrow()

        assertEquals(emptyList<Error>(), result)
        assertEquals(chunk.view(flowId, requester).orThrow(), fields)
    }

    @Test
    fun `it removes submitted value`() {
        val chunk = FieldsChunk(Storage())
            .also {
                it.submit(flowId, requester, fields)
                    .orThrow()
            }


        val result = chunk
            .clear(flowId, requester)
            .orThrow()

        assertEquals(emptyList<Error>(), result)
        assertNull(chunk.view(flowId, requester).orThrow())
    }
}
