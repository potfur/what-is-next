package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap

class PrefilledChunk<ID, T : Specification.Type, S : Specification<T>, R, D, F>(
    private val delegate: DataChunk<ID, T, S, R, D, F>,
    private val source: (ID, R) -> Result4k<D?, F>
) : DataChunk<ID, T, S, R, D, F> by delegate {
    override fun view(flowId: ID, requester: R): Result4k<D?, F> = delegate.view(flowId, requester)
        .flatMap {
            if (it == null) source(flowId, requester)
            else Success(it)
        }
}
