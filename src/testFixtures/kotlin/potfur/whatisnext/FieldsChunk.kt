package potfur.whatisnext

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import potfur.whatisnext.Error.Type.MISSING
import potfur.whatisnext.FieldsSpec.Requirement.OPTIONAL
import potfur.whatisnext.FieldsSpec.Requirement.REQUIRED

data object FieldType : Specification.Type

data class Fields(val firstName: String?, val lastName: String?, val email: String?)

data object FieldsRequirements {
    val firstName = OPTIONAL
    val lastName = OPTIONAL
    val email = REQUIRED
}

data class FieldsSpec(
    override val state: Specification.State,
    val requirements: FieldsRequirements
) : Specification<FieldType> {

    override val type: FieldType = FieldType

    enum class Requirement {
        OPTIONAL,
        REQUIRED,
    }
}

class FieldsChunk(
    private val storage: Storage<Id, Fields, Exception>
) : MutableDataChunk<Id, FieldType, FieldsSpec, Requester, Fields, Exception> {
    override val type: FieldType = FieldType

    override fun spec(flowId: Id, requester: Requester) =
        storage.fetch(flowId).map { FieldsSpec(Specification.State { it != null }, FieldsRequirements) }

    override fun view(flowId: Id, requester: Requester) =
        storage.fetch(flowId)

    override fun validate(flowId: Id, requester: Requester, data: Fields?) =
        Success(
            listOfNotNull(
                FieldsRequirements.firstName.validate("firstName", data?.firstName),
                FieldsRequirements.lastName.validate("lastName", data?.lastName),
                FieldsRequirements.email.validate("email", data?.email),
            )
        )

    override fun submit(flowId: Id, requester: Requester, data: Fields?): Result4k<List<ValidationError>, Exception> =
        validate(flowId, requester, data)
            .flatMap {
                if (it.isEmpty() && data != null) storage.store(flowId, data).flatMap { Success(emptyList()) }
                else Success(it)
            }

    override fun clear(flowId: Id, requester: Requester): Result4k<List<ValidationError>, Exception> =
        storage.clear(flowId).map { emptyList() }

    private fun FieldsSpec.Requirement.validate(name: String, value: String?) =
        when (this) {
            REQUIRED -> if (value == null) Error(name, MISSING) else null
            else -> null
        }
}
