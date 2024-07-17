package potfur.whatisnext

import dev.forkhandles.result4k.Success
import potfur.whatisnext.Specification.Type

data class StubType(val type: String) : Type {
    override val name = this.name()
}

data class StubSpec(override val type: StubType, override val state: Specification.State) : Specification<StubType>

fun StubChunk(type: String, state: Specification.State, value: String? = null) =
    object : DataChunk<Id, StubType, StubSpec, Requester, String, Exception> {
        override val type = StubType(type)
        override fun spec(flowId: Id, requester: Requester) = Success(StubSpec(this.type, state))
        override fun view(flowId: Id, requester: Requester) = Success(value)
    }
