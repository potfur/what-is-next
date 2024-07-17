package potfur.whatisnext

import dev.forkhandles.values.IntValue
import dev.forkhandles.values.IntValueFactory


class Id private constructor(override val value: Int): IntValue(value) {
    companion object : IntValueFactory<Id>(::Id)
}
