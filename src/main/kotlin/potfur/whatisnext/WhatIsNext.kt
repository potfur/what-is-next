package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import potfur.whatisnext.Specification.Type

interface WhatIsNext<ID, T : Type, S : Specification<out T>, R, F> {
    fun whatIsNext(flowId: ID, requester: R): Result4k<List<S>, F>

    fun isCompleted(flowId: ID, requester: R): Result4k<Boolean, F> =
        whatIsNext(flowId, requester).map { spec: List<S> -> spec.all { it.isComplete() } }
}

typealias DataChunks<ID, T, S, R, F> = List<DataChunk<ID, out T, out S, R, out Any?, F>>
typealias GenericDataChunk<ID, R, F> = DataChunk<ID, out Type, out Specification<out Type>, R, out Any?, F>

fun interface ChunkAggregate<ID, T : Type, S : Specification<out T>, R, F> {
    operator fun invoke(
        chunks: List<GenericDataChunk<ID, R, F>>,
        id: ID,
        requester: R
    ): Result4k<DataChunks<ID, T, S, R, F>, F>
}

class ChunkAggregateWhatIsNext<ID, T : Type, S : Specification<out T>, R, F>(
    private val chunks: List<ChunkAggregate<ID, T, S, R, F>>
) : WhatIsNext<ID, T, S, R, F> {
    companion object {
        operator fun <ID, T : Type, S : Specification<out T>, R, F> invoke(
            vararg chunks: ChunkAggregate<ID, T, S, R, F>
        ) = ChunkAggregateWhatIsNext(chunks.toList())

        operator fun <ID, T : Type, S : Specification<out T>, R, F> invoke(
            vararg chunks: DataChunk<ID, out T, out S, R, out Any?, F>
        ) = ChunkAggregateWhatIsNext(chunks.map {
            ChunkAggregate { _, _, _ -> Success(listOf(it)) }
        })
    }

    override fun whatIsNext(flowId: ID, requester: R): Result4k<List<S>, F> =
        chunks.fold(Success(emptyList())) { acc: Result4k<List<DataChunk<ID, out T, out S, R, out Any?, F>>, F>, c ->
            acc.flatMap { a -> c(emptyList(), flowId, requester).map { a + it } }
        }.flatMap {
            it.fold(Success(emptyList())) { acc: Result4k<List<S>, F>, c ->
                acc.flatMap { a -> c.spec(flowId, requester).map { s -> a + s } }
            }
        }
}

fun <ID, R, F> GenericDataChunk<ID, R, F>.on(
    fn: (ID, R) -> Result4k<Boolean, F>
): ChunkAggregate<ID, out Type, out Specification<out Type>, R, F> =
    ChunkAggregate { _, id, r ->
        fn(id, r).map { if (it) listOf(this) else emptyList() }
    }

fun <ID, R, F> GenericDataChunk<ID, R, F>.then(
    vararg chunks: GenericDataChunk<ID, R, F>
): ChunkAggregate<ID, out Type, out Specification<out Type>, R, F> =
    ChunkAggregate { _, id, r ->
        spec(id, r)
            .map { if (it.isComplete()) chunks.toList() else emptyList() }
            .map { listOf(this) + it }
    }

fun <ID, R, F> List<GenericDataChunk<ID, R, F>>.then(
    vararg chunks: GenericDataChunk<ID, R, F>
): ChunkAggregate<ID, out Type, out Specification<out Type>, R, F> =
    ChunkAggregate { _, id, r ->
        fold(Success(true)) { acc: Result4k<Boolean, F>, c ->
            acc.flatMap { a -> c.spec(id, r).map { a && it.isComplete() } }
        }
            .map { if (it) chunks.toList() else emptyList() }
            .map { this + it }
    }

fun <ID, R, F> GenericDataChunk<ID, R, F>.then(
    vararg chunks: ChunkAggregate<ID, out Type, out Specification<out Type>, R, F>
): ChunkAggregate<ID, out Type, out Specification<out Type>, R, F> =
    ChunkAggregate { _, id, r ->
        spec(id, r)
            .flatMap {
                if (it.isComplete())
                    chunks.fold(Success(emptyList())) { acc: Result4k<List<GenericDataChunk<ID, R, F>>, F>, c ->
                        acc.flatMap { c(listOf(this), id, r) }
                    }
                else Success(emptyList())
            }
            .map { listOf(this) + it }
    }

fun <V, ID, R, F> branch(value: V, chunk: GenericDataChunk<ID, R, F>) =
    value to ChunkAggregate { _, _, _ -> Success(listOf(chunk)) }

fun <V, ID, R, F> branch(value: V, vararg chunks: GenericDataChunk<ID, R, F>) =
    value to ChunkAggregate { _, _, _ -> Success(chunks.toList()) }

fun <V, ID, R, F> branch(value: V, chunks: ChunkAggregate<ID, out Type, out Specification<out Type>, R, F>) =
    value to chunks

fun <ID, T : Type, S : Specification<T>, R, D, F, V> DataChunk<ID, T, S, R, D, F>.thenOnValue(
    vararg branches: Pair<V, ChunkAggregate<ID, out Type, out Specification<out Type>, R, F>>,
    fn: (D?) -> V
): ChunkAggregate<ID, out Type, out Specification<out Type>, R, F> = ChunkAggregate { _, id, r ->
    spec(id, r)
        .flatMap {
            if (!it.isComplete()) Success(emptyList())
            else view(id, r).flatMap { v ->
                branches.toMap()[fn(v)]?.let { it(listOf(this), id, r) }
                    ?: Success(emptyList())
            }
        }
        .map { listOf(this) + it }
}
