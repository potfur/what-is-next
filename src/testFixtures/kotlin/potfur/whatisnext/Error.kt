package potfur.whatisnext

data class Error(val key: String, val value: Type) : ValidationError {
    enum class Type {
        MISSING,
        INVALID,
    }
}
