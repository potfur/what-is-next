package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

interface Storage<ID, D, F> {
    fun fetch(id: ID): Result4k<D?, F>
    fun store(id: ID, value: D): Result4k<Unit, F>
    fun clear(id: ID): Result4k<Unit, F>
}

fun <ID, D, F> Storage(): Storage<ID, D, F> = object : Storage<ID, D, F> {
    val stored = mutableMapOf<ID, D>()
    override fun fetch(id: ID) = Success(stored[id])

    override fun store(id: ID, value: D) = Success(Unit).also { stored[id] = value }

    override fun clear(id: ID): Result4k<Unit, F> = Success(Unit).also { stored.remove(id) }
}
