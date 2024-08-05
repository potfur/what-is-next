package potfur.whatisnext.adapter

import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.ContractRouteSpec1
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import potfur.whatisnext.DataChunk
import potfur.whatisnext.MutableDataChunk
import potfur.whatisnext.Specification
import potfur.whatisnext.Specification.Type

open class ChunkHttpAdapter<ID, T : Type, S : Specification<T>, R, D, F>(
    private val basePath: ContractRouteSpec1<ID>,
    private val chunk: DataChunk<ID, T, S, R, D, F>,
    private val injector: (Response, D?) -> Response,
    private val requesterResolver: (Request) -> R,
) : Iterable<ContractRoute> {
    fun view() =
        basePath / chunk.type.name bindContract GET to { id, _ ->
            { request ->
                chunk.view(id, requesterResolver(request))
                    .map { injector(Response(OK), it) }
                    .mapFailure { Response(NOT_FOUND) }
                    .get()
            }
        }

    override fun iterator() = listOf(view()).iterator()
}

open class MutableChunkHttpAdapter<ID, T : Type, S : Specification<T>, R, D, E, F>(
    private val basePath: ContractRouteSpec1<ID>,
    private val chunk: MutableDataChunk<ID, T, S, R, D, E, F>,
    private val injector: (Response, D?) -> Response,
    private val extractor: (Request) -> D?,
    private val errors: (Response, List<E>) -> Response,
    private val requesterResolver: (Request) -> R,
) : ChunkHttpAdapter<ID, T, S, R, D, F>(basePath, chunk, injector, requesterResolver), Iterable<ContractRoute> {
    fun validate() =
        basePath / chunk.type.name / "validate" bindContract PUT to { id, _, _ ->
            { request ->
                chunk.validate(id, requesterResolver(request), extractor(request))
                    .map { errors(Response(OK), it) }
                    .mapFailure { Response(NOT_FOUND) }
                    .get()
            }
        }

    fun submit() =
        basePath / chunk.type.name bindContract POST to { id, _ ->
            { request ->
                chunk.submit(id, requesterResolver(request), extractor(request))
                    .map { errors(Response(OK), it) }
                    .mapFailure { Response(NOT_FOUND) }
                    .get()
            }
        }

    fun delete() =
        basePath / chunk.type.name bindContract DELETE to { id, _ ->
            { request ->
                chunk.clear(id, requesterResolver(request))
                    .map { errors(Response(OK), it) }
                    .mapFailure { Response(NOT_FOUND) }
                    .get()
            }
        }

    override fun iterator() = listOf(view(), validate(), submit(), delete()).iterator()
}
