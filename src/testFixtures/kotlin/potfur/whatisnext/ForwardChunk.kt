package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import potfur.whatisnext.Specification.State
import potfur.whatisnext.Specification.Type

data object ForwardType : Type

data class ForwardSpec<ID>(val id: ID, override val state: State) : Specification<ForwardType> {
    override val type = ForwardType
}

fun <ID> ForwardChunk(
    targetFn: (Id, Requester) -> Result4k<ForwardSpec<ID>, Exception>,
    viewFn: (Id, Requester) -> Result4k<ID, Exception>
) = object :
    DataChunk<Id, ForwardType, ForwardSpec<ID>, Requester, ID, Exception> {
    override val type = ForwardType
    override fun spec(flowId: Id, requester: Requester) = targetFn(flowId, requester)
    override fun view(flowId: Id, requester: Requester) = viewFn(flowId, requester)
}

fun <TID, T : Type, S : Specification<out T>> ForwardChunk(targetId: TID, target: WhatIsNext<Id, T, S, Requester, Exception>) =
    ForwardChunk(
        { id, r -> target.isCompleted(id, r).map { ForwardSpec(targetId, State { it }) } },
        { _, _ -> Success(targetId) }
    )
