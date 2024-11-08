package potfur.whatisnext.adapter

import dev.forkhandles.result4k.Result4k
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
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import potfur.whatisnext.DataChunk
import potfur.whatisnext.MutableDataChunk
import potfur.whatisnext.Specification
import potfur.whatisnext.Specification.Type

typealias ChunkHandler<ID> = (ID, Request) -> Response

fun <ID, T : Type, S : Specification<T>, R, D, F> viewContractRoute(
    basePath: ContractRouteSpec1<ID>,
    chunk: DataChunk<ID, T, S, R, D, F>,
    handler: ChunkHandler<ID>,
) = basePath / chunk.type.name bindContract GET to { id, _ ->
    { request -> handler(id, request) }
}

fun <ID, T : Type, S : Specification<T>, R, D, F> validateContractRoute(
    basePath: ContractRouteSpec1<ID>,
    chunk: DataChunk<ID, T, S, R, D, F>,
    handler: ChunkHandler<ID>,
) = basePath / chunk.type.name / "validate" bindContract PUT to { id, _, _ ->
    { request -> handler(id, request) }
}

fun <ID, T : Type, S : Specification<T>, R, D, F> submitContractRoute(
    basePath: ContractRouteSpec1<ID>,
    chunk: DataChunk<ID, T, S, R, D, F>,
    handler: ChunkHandler<ID>,
) = basePath / chunk.type.name bindContract POST to { id, _ ->
    { request -> handler(id, request) }
}

fun <ID, T : Type, S : Specification<T>, R, D, F> deleteContractRoute(
    basePath: ContractRouteSpec1<ID>,
    chunk: DataChunk<ID, T, S, R, D, F>,
    handler: ChunkHandler<ID>,
) = basePath / chunk.type.name bindContract DELETE to { id, _ ->
    { request -> handler(id, request) }
}

@JvmName("asValueHttpHandler")
fun <ID, T : Type, S : Specification<T>, R, D, F> DataChunk<ID, T, S, R, D, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    requesterResolver: (Request) -> R,
    payloadLens: BiDiBodyLens<DataEnvelope.Value<D?>>,
) = asHttpAdapter(basePath, requesterResolver, { r, v -> r.with(payloadLens of DataEnvelope.Value(v)) })

@JvmName("asStructuredHttpHandler")
fun <ID, T : Type, S : Specification<T>, R, D, F> DataChunk<ID, T, S, R, D, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    requesterResolver: (Request) -> R,
    payloadLens: BiDiBodyLens<DataEnvelope.Structured<D?>>,
) = asHttpAdapter(basePath, requesterResolver, { r, v -> r.with(payloadLens of DataEnvelope.Structured(v)) })

fun <ID, T : Type, S : Specification<T>, R, D, F> DataChunk<ID, T, S, R, D, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    requesterResolver: (Request) -> R,
    dataInjector: (Response, D?) -> Response,
): Iterable<ContractRoute> = listOf(
    viewContractRoute(basePath, this) { id, request ->
        view(id, requesterResolver(request))
            .map { dataInjector(Response(OK), it) }
            .mapFailure { Response(NOT_FOUND) }
            .get()
    }
)

@JvmName("asValueHttpHandler")
fun <ID, T : Type, S : Specification<T>, R, D, E, F> MutableDataChunk<ID, T, S, R, D, E, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    requesterResolver: (Request) -> R,
    payloadLens: BiDiBodyLens<DataEnvelope.Value<D?>>,
    validationLens: BiDiBodyLens<DataEnvelope.Structured<List<E>>>,
) = asHttpAdapter(
    basePath = basePath,
    requesterResolver = requesterResolver,
    dataInjector = { r, v -> r.with(payloadLens of DataEnvelope.Value(v)) },
    errorsInjector = { r, e -> r.with(validationLens of DataEnvelope.Structured(e)) },
    dataExtractor = { r -> payloadLens(r).data.value }
)

@JvmName("asStructuredHttpHandler")
fun <ID, T : Type, S : Specification<T>, R, D, E, F> MutableDataChunk<ID, T, S, R, D, E, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    requesterResolver: (Request) -> R,
    payloadLens: BiDiBodyLens<DataEnvelope.Structured<D?>>,
    validationLens: BiDiBodyLens<DataEnvelope.Structured<List<E>>>,
) = asHttpAdapter(
    basePath = basePath,
    requesterResolver = requesterResolver,
    dataInjector = { r, v -> r.with(payloadLens of DataEnvelope.Structured(v)) },
    errorsInjector = { r, e -> r.with(validationLens of DataEnvelope.Structured(e)) },
    dataExtractor = { r -> payloadLens(r).data }
)

fun <ID, T : Type, S : Specification<T>, R, D, E, F> MutableDataChunk<ID, T, S, R, D, E, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    requesterResolver: (Request) -> R,
    dataInjector: (Response, D?) -> Response,
    errorsInjector: (Response, List<E>) -> Response,
    dataExtractor: (Request) -> D?,
): Iterable<ContractRoute> = listOf(
    viewContractRoute(basePath, this) { id, request ->
        view(id, requesterResolver(request))
            .asResponse(dataInjector)
    },
    validateContractRoute(basePath, this) { id, request ->
        validate(id, requesterResolver(request), dataExtractor(request))
            .asErrorResponse(errorsInjector)
    },
    submitContractRoute(basePath, this) { id, request ->
        submit(id, requesterResolver(request), dataExtractor(request))
            .asErrorResponse(errorsInjector)
    },
    deleteContractRoute(basePath, this) { id, request ->
        clear(id, requesterResolver(request))
            .asErrorResponse(errorsInjector)
    },
)

private fun <T, F> Result4k<T, F>.asResponse(injector: (Response, T) -> Response) =
    map { injector(Response(OK), it) }
        .mapFailure { Response(NOT_FOUND) }
        .get()

private fun <T, F> Result4k<List<T>, F>.asErrorResponse(injector: (Response, List<T>) -> Response) =
    map { injector(Response(OK), it) }
        .mapFailure { Response(NOT_FOUND) }
        .get()
