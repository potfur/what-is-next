package potfur.whatisnext

import potfur.whatisnext.Specification.Type

internal fun <T : Type> T.name(prefix: String = "", postfix: String = "Type") =
    this::class.simpleName.let { it?.substring(prefix.length, it.length - postfix.length) ?: "unnamed" }
        .replace("([A-Z])".toRegex(), "-$1")
        .lowercase()
        .trim('-')
