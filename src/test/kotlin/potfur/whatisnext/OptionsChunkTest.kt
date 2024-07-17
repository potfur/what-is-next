package potfur.whatisnext

import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import potfur.whatisnext.Error.Type.INVALID
import potfur.whatisnext.Error.Type.MISSING
import potfur.whatisnext.Option.Companion.A
import potfur.whatisnext.Option.Companion.B
import potfur.whatisnext.Specification.State.REQUIRED

class OptionsChunkTest : ChunkTestCase() {
    private val options = listOf(A, B)

    @Test
    fun `it shows options in specification`() {
        val result = OptionsChunk(Storage(), options)
            .spec(flowId, requester)
            .orThrow()

        assertEquals(OptionSpec(REQUIRED, options), result)
    }

    @Test
    fun `it submitted defined value`() {
        val chunk = OptionsChunk(Storage(), options)
        assertNull(chunk.view(flowId, requester).orThrow())

        chunk.submit(flowId, requester, options.first())
        assertEquals(options.first(), chunk.view(flowId, requester).orThrow())
    }

    @Test
    fun `it validates as invalid when option is missing`() {
        val result = OptionsChunk(Storage(), options)
            .validate(flowId, requester, null)
            .orThrow()

        assertEquals(listOf(Error("data", MISSING)), result)
    }

    @Test
    fun `it validates as invalid when option is invalid`() {
        val result = OptionsChunk(Storage(), options)
            .validate(flowId, requester, Option.of("anything"))
            .orThrow()

        assertEquals(listOf(Error("data", INVALID)), result)
    }

    @Test
    fun `it validates with no errors when option is valid`() {
        val result = OptionsChunk(Storage(), options)
            .validate(flowId, requester, options.first())
            .orThrow()

        assertEquals(emptyList<Error>(), result)
    }

    @Test
    fun `it rejects submit when option is missing`() {
        val result = OptionsChunk(Storage(), options)
            .submit(flowId, requester, null)
            .orThrow()

        assertEquals(listOf(Error("data", MISSING)), result)
    }

    @Test
    fun `it rejects submit when option is invalid`() {
        val result = OptionsChunk(Storage(), options)
            .submit(flowId, requester, Option.of("anything"))
            .orThrow()

        assertEquals(listOf(Error("data", INVALID)), result)
    }

    @Test
    fun `it submits with no errors when option is valid`() {
        val chunk = OptionsChunk(Storage(), options)
        val result = chunk
            .submit(flowId, requester, options.first())
            .orThrow()

        assertEquals(emptyList<Error>(), result)
        assertEquals(chunk.view(flowId, requester).orThrow(), options.first())
    }

    @Test
    fun `it removes submitted value`() {
        val chunk = OptionsChunk(Storage(), options)
            .also {
                it.submit(flowId, requester, options.first())
                    .orThrow()
            }


        val result = chunk
            .clear(flowId, requester)
            .orThrow()

        assertEquals(emptyList<Error>(), result)
        assertNull(chunk.view(flowId, requester).orThrow())
    }
}
