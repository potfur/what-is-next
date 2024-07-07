package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import potfur.whatisnext.Specification.State
import potfur.whatisnext.Specification.Type

data object ReadOnlyType : Type

data object ReadOnlySpec : Specification<ReadOnlyType> {
    override val state = State.COMPLETED
    override val type = ReadOnlyType
}

fun ReadOnlyChunk(viewFn: (Id, Requester) -> Result4k<String, Exception>) =
    object : DataChunk<Id, ReadOnlyType, ReadOnlySpec, Requester, String, Exception> {
        override val type = ReadOnlyType
        override fun spec(flowId: Id, requester: Requester) = Success(ReadOnlySpec)
        override fun view(flowId: Id, requester: Requester) = viewFn(flowId, requester)
    }
