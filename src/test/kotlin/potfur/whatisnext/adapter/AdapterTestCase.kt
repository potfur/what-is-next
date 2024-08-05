package potfur.whatisnext.adapter

import org.http4k.contract.ContractRoute
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.Path
import org.http4k.lens.value
import org.http4k.routing.RoutingHttpHandler
import potfur.whatisnext.Id
import potfur.whatisnext.Requester

open class AdapterTestCase {
    val flowId = Id.of(1)
    val basePath = "foo" / Path.value(Id).of("flowId")
    val requesterResolver = { _: Request -> Requester(2) }

    fun Iterable<ContractRoute>.asRoutingHttpAdapter(): RoutingHttpHandler = contract { routes += toList() }

    fun <T> Response.handleOrThrow(lens: BiDiBodyLens<T>): T = let {
        when (it.status) {
            OK -> lens(it)
            else -> throw Exception("${it.status}, ${it.bodyString()}")
        }
    }
}
