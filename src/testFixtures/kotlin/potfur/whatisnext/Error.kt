package potfur.whatisnext

data class Error(val key: String, val value: Type) {
    enum class Type {
        MISSING,
        INVALID,
    }
}
