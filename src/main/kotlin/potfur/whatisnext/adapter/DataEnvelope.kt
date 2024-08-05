package potfur.whatisnext.adapter

sealed interface DataEnvelope {
    data class Structured<T>(val data: T) : DataEnvelope

    data class Value<T>(val data: ValueKey<T>) : DataEnvelope {
        data class ValueKey<T>(val value: T)

        constructor(value: T) : this(ValueKey(value))
    }
}
