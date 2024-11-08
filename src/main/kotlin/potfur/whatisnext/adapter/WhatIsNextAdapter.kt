package potfur.whatisnext.adapter

import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.ContractRouteSpec1
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import potfur.whatisnext.Specification
import potfur.whatisnext.Specification.Type
import potfur.whatisnext.WhatIsNext

open class WhatIsNextAdapter<ID, T : Type, S : Specification<out T>, R, F>(
    private val basePath: ContractRouteSpec1<ID>,
    private val flow: WhatIsNext<ID, T, S, R, F>,
    private val specInjector: (Response, List<S>) -> Response,
    private val statusInjector: (Response, Boolean) -> Response,
    private val requesterResolver: (Request) -> R,
) : Iterable<ContractRoute> {
    fun next() =
        basePath / "next" bindContract Method.GET to { id, _ ->
            { request ->
                flow.whatIsNext(id, requesterResolver(request))
                    .map { specInjector(Response(OK), it) }
                    .mapFailure { Response(NOT_FOUND) }
                    .get()
            }
        }

    fun status() = basePath / "status" bindContract Method.GET to { id, _ ->
        { request ->
            flow.isCompleted(id, requesterResolver(request))
                .map { statusInjector(Response(OK), it) }
                .mapFailure { Response(NOT_FOUND) }
                .get()
        }
    }

    override fun iterator() = listOf(next(), status()).iterator()
}

fun <ID, T : Type, S : Specification<out T>, R, F> WhatIsNext<ID, T, S, R, F>.asHttpAdapter(
    basePath: ContractRouteSpec1<ID>,
    specLens: BiDiBodyLens<DataEnvelope.Structured<List<Specification<out Type>>>>,
    statusLens: BiDiBodyLens<DataEnvelope.Value<Boolean>>,
    requesterResolver: (Request) -> R,
) = WhatIsNextAdapter(
    basePath = basePath,
    flow = this,
    specInjector = { r, v -> r.with(specLens of DataEnvelope.Structured(v)) },
    statusInjector = { r, v -> r.with(statusLens of DataEnvelope.Value(v)) },
    requesterResolver = requesterResolver,
)
