package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.valueOrNull
import potfur.whatisnext.Error.Type.INVALID
import potfur.whatisnext.Error.Type.MISSING

data object OptionType : Specification.Type

data class OptionSpec(override val state: Specification.State, val options: List<String>) :
    Specification<OptionType> {
    override val type: OptionType = OptionType
}

class OptionsChunk(
    private val storage: Storage<Id, String, Exception>,
    private val availableOptions: List<String>,
) : MutableDataChunk<Id, OptionType, OptionSpec, Requester, String, Exception> {
    override val type: OptionType = OptionType

    override fun spec(flowId: Id, requester: Requester) =
        Success(OptionSpec(Specification.State { storage.fetch(flowId).valueOrNull() != null }, availableOptions))

    override fun view(flowId: Id, requester: Requester) =
        storage.fetch(flowId)

    override fun validate(flowId: Id, requester: Requester, data: String?) =
        Success(
            when (data) {
                null -> listOf(Error("data", MISSING))
                !in availableOptions -> listOf(Error("data", INVALID))
                else -> emptyList()
            }
        )

    override fun submit(flowId: Id, requester: Requester, data: String?): Result4k<List<ValidationError>, Exception> =
        validate(flowId, requester, data)
            .flatMap {
                if (it.isEmpty() && data != null) storage.store(flowId, data).flatMap { Success(emptyList()) }
                else Success(it)
            }

    override fun clear(flowId: Id, requester: Requester): Result4k<List<ValidationError>, Exception> =
        storage.clear(flowId).map { emptyList() }
}
