package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import potfur.whatisnext.Error.Type.INVALID
import potfur.whatisnext.Error.Type.MISSING
import potfur.whatisnext.Specification.Type

data object OptionType : Type {
    override val name = this.name()
}

class Option private constructor(override val value: String): StringValue(value) {
    companion object : NonEmptyStringValueFactory<Option>(::Option) {
        val A = of("A")
        val B = of("B")
    }
}

data class OptionSpec(override val state: Specification.State, val options: List<Option>) :
    Specification<OptionType> {
    override val type: OptionType = OptionType
}

class OptionsChunk(
    private val storage: Storage<Id, Option, Exception>,
    private val availableOptions: List<Option>,
) : MutableDataChunk<Id, OptionType, OptionSpec, Requester, Option, Error, Exception> {
    override val type: OptionType = OptionType

    override fun spec(flowId: Id, requester: Requester) =
        Success(OptionSpec(Specification.State { storage.fetch(flowId).valueOrNull() != null }, availableOptions))

    override fun view(flowId: Id, requester: Requester) =
        storage.fetch(flowId)

    override fun validate(flowId: Id, requester: Requester, data: Option?) =
        Success(
            when (data) {
                null -> listOf(Error("data", MISSING))
                !in availableOptions -> listOf(Error("data", INVALID))
                else -> emptyList()
            }
        )

    override fun submit(flowId: Id, requester: Requester, data: Option?): Result4k<List<Error>, Exception> =
        validate(flowId, requester, data)
            .flatMap {
                if (it.isEmpty() && data != null) storage.store(flowId, data).flatMap { Success(emptyList()) }
                else Success(it)
            }

    override fun clear(flowId: Id, requester: Requester): Result4k<List<Error>, Exception> =
        storage.clear(flowId).map { emptyList() }
}
