package potfur.whatisnext.adapter

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.value
import org.http4k.format.withStandardMappings
import org.http4k.lens.BiDiMapping
import potfur.whatisnext.FieldType
import potfur.whatisnext.ForwardType
import potfur.whatisnext.Id
import potfur.whatisnext.Option
import potfur.whatisnext.OptionType
import potfur.whatisnext.ReadOnlyType
import potfur.whatisnext.Specification
import potfur.whatisnext.StubType

val TestingJson = ConfigurableJackson(
    KotlinModule.Builder().build()
        .asConfigurable()
        .withStandardMappings()
        .apply {
            value(Id)
            value(Option)
            text(
                BiDiMapping<String, Specification.Type>(
                    {
                        when(it) {
                            FieldType.name -> FieldType
                            ForwardType.name -> ForwardType
                            OptionType.name -> OptionType
                            ReadOnlyType.name -> ReadOnlyType
                            else -> StubType(it)
                        }
                    },
                    { it.name }
                )
            )
        }
        .done()
        .deactivateDefaultTyping()
        .configure(FAIL_ON_NULL_FOR_PRIMITIVES, true)
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(FAIL_ON_IGNORED_PROPERTIES, false)
)

