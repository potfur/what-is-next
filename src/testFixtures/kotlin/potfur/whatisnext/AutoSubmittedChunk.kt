package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map

class AutoSubmittedChunk<ID, T : Specification.Type, S : Specification<T>, R, D, E, F>(
    private val delegate: MutableDataChunk<ID, T, S, R, D, E, F>,
    private val fn: (DataChunk<ID, T, S, R, D, F>, ID, R) -> Result4k<D?, F>
) : MutableDataChunk<ID, T, S, R, D, E, F> by delegate {
    override fun view(flowId: ID, requester: R): Result4k<D?, F> =
        delegate.view(flowId, requester).flatMap {
            if (it != null) Success(it)
            else fn(delegate, flowId, requester).flatMap { v ->
                if (v != null) delegate.submit(flowId, requester, v).map { v }
                else Success(v)
            }
        }
}
