package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import potfur.whatisnext.Specification.State.COMPLETED
import potfur.whatisnext.Specification.State.OPTIONAL
import potfur.whatisnext.Specification.Type


interface Specification<T : Type> {
    val type: T
    val state: State

    interface Type {
        val name: String
    }

    enum class State {
        COMPLETED,
        OPTIONAL,
        REQUIRED;

        companion object {
            operator fun invoke(initial: State = REQUIRED, fn: () -> Boolean) = if (fn()) COMPLETED else initial
        }
    }

}

fun <T : Type> Specification<T>.isComplete() = state in listOf(COMPLETED, OPTIONAL)

interface DataChunk<ID, T : Type, S : Specification<T>, R, D, F> {
    val type: T
    fun spec(flowId: ID, requester: R): Result4k<S, F>
    fun view(flowId: ID, requester: R): Result4k<D?, F>
}

interface MutableDataChunk<ID, T : Type, S : Specification<T>, R, D, E, F> : DataChunk<ID, T, S, R, D, F> {
    fun validate(flowId: ID, requester: R, data: D?): Result4k<List<E>, F>
    fun submit(flowId: ID, requester: R, data: D?): Result4k<List<E>, F>
    fun clear(flowId: ID, requester: R): Result4k<List<E>, F>
}
