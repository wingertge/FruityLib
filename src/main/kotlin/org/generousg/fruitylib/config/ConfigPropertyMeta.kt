package org.generousg.fruitylib.config

import com.google.common.base.CharMatcher
import com.google.common.base.Strings
import com.google.common.base.Throwables
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property
import org.apache.commons.lang3.StringUtils
import org.generousg.fruitylib.util.ConfigTypes
import org.generousg.fruitylib.util.Log
import org.generousg.fruitylib.util.StringConversionException
import org.generousg.fruitylib.util.TypeRW
import org.generousg.fruitylib.util.io.IStringSerializer
import java.lang.reflect.Field
import java.util.*


abstract class ConfigPropertyMeta protected constructor(config: Configuration, val field: Field, annotation: ConfigProperty) {
    enum class Result { CANCELLED, ONLINE, OFFLINE }

    companion object {
        fun createMetaForField(config: Configuration, field: Field): ConfigPropertyMeta? {
            val annotation = field.getAnnotation(ConfigProperty::class.java) ?: return null
            val fieldType = field.type
            return if(fieldType.isArray) MultipleValues(config, field, annotation) else SingleValue(config, field, annotation)
        }
    }

    val name: String
    val category: String = annotation.category
    val comment: String = annotation.comment
    val type: Property.Type
    private val online: Boolean
    private val defaultValue: Any
    private val defaultText: Array<String>

    protected var propertyText = arrayOf<String>()

    protected val converter: IStringSerializer<*>
    protected val wrappedProperty: Property

    init {
        val mod = field.getAnnotation(OnlineModifiable::class.java)
        online = mod != null
        var name = annotation.name
        if(Strings.isNullOrEmpty(name)) name = field.name
        this.name = name
        defaultValue = getFieldValue()
        requireNotNull(defaultValue) { "Config field $name has no default value." }
        defaultText = convertToStringArray(defaultValue)
        val fieldType = getFieldType()
        type = ConfigTypes.CONFIG_TYPES[fieldType] ?: Property.Type.INTEGER
        requireNotNull(type) { "Config field $name has no property type mapping" }
        converter = TypeRW.STRING_SERIALIZERS[fieldType] as IStringSerializer<*>
        requireNotNull(converter) { "Config field $name has no known conversion from string" }
        wrappedProperty = getProperty(config, type, defaultValue)
    }

    fun updateValueFromConfig(force: Boolean) {
        if(!force && !wrappedProperty.wasRead() && !wrappedProperty.isList) return

        val actualType = wrappedProperty.type
        require(type == actualType) { "Invalid config property type $actualType, expected $type" }

        val currentValue = getActualPropertyValue()
        try {
            val converted = convertValue(*currentValue)
            setFieldValue(converted)
        } catch (e: StringConversionException) {
            Log.warn(e, "Invalid config property value ${Arrays.toString(currentValue)}, using default value")
        }
    }

    protected fun setFieldValue(value: Any?) {
        try {
            field.set(null, value)
        } catch (e: Throwable) {
            throw Throwables.propagate(e)
        }
    }

    protected fun getFieldValue(): Any {
        try {
            return field.get(null)
        } catch (e: Throwable) {
            throw Throwables.propagate(e)
        }
    }

    protected abstract fun getFieldType(): Class<out Any>
    protected abstract fun getProperty(configFile: Configuration, expectedType: Property.Type, defaultValue: Any): Property

    fun getPropertyValue(): Array<String> = propertyText
    abstract fun getActualPropertyValue(): Array<String>
    protected abstract fun setPropertyValue(vararg values: String)
    protected abstract fun convertValue(vararg values: String): Any?

    abstract fun acceptsMultipleValues(): Boolean
    abstract fun valueDescription(): String

    protected abstract fun convertToStringArray(value: Any): Array<String>

    @Suppress("UNCHECKED_CAST")
    fun tryChangeValue(vararg proposedValues: String): Result {
        val event = ConfigurationChange.Pre(name, category, proposedValues as Array<String>)
        if(MinecraftForge.EVENT_BUS.post(event)) return Result.CANCELLED

        val converted = convertValue(*event.proposedValues)
        if(online) setFieldValue(converted)
        MinecraftForge.EVENT_BUS.post(ConfigurationChange.Post(name, category))
        setPropertyValue(*event.proposedValues)
        propertyText = event.proposedValues

        return if(online) Result.ONLINE else Result.OFFLINE
    }

    fun defaultValues(): Array<String> = defaultText.clone()
    fun getProperty(): Property = wrappedProperty

    private class SingleValue(config: Configuration, field: Field, annotation: ConfigProperty) : ConfigPropertyMeta(config, field, annotation) {
        init {
            propertyText = arrayOf(wrappedProperty.string)
        }

        override fun getProperty(configFile: Configuration, expectedType: Property.Type, defaultValue: Any): Property {
            val defaultString = defaultValue.toString()
            return configFile.get(category, name, defaultString, comment, expectedType)
        }

        override fun getActualPropertyValue(): Array<String> = arrayOf(wrappedProperty.string)
        override fun setPropertyValue(vararg values: String) {
            require(values.size == 1) { "This parameter has only one value" }
            wrappedProperty.set(values[0])
        }

        override fun convertValue(vararg values: String): Any? {
            require(values.size == 1) { "This parameter has only one value" }
            val value = values[0]
            return converter.readFromString(value)
        }

        override fun acceptsMultipleValues(): Boolean = false
        override fun valueDescription(): String = propertyText[0]
        override fun convertToStringArray(value: Any): Array<String> = arrayOf(value.toString())
        override fun getFieldType(): Class<out Any> = field.type
    }

    private open class MultipleValues (config: Configuration, field: Field, annotation: ConfigProperty): ConfigPropertyMeta(config, field, annotation) {
        override fun getFieldType(): Class<out Any> = field.type.componentType

        override fun getProperty(configFile: Configuration, expectedType: Property.Type, defaultValue: Any): Property {
            val defaultStrings = convertToStringArray(defaultValue)
            return configFile.get(category, name, defaultStrings, comment, expectedType)
        }

        override fun getActualPropertyValue(): Array<String> = wrappedProperty.stringList
        override fun setPropertyValue(vararg values: String) = wrappedProperty.set(values)

        override fun convertValue(vararg values: String): Any? {
            val matcher = CharMatcher.`is`('"')
            val result = arrayListOf<Any?>()
            for (i in 0..values.size-1) {
                val value = matcher.trimFrom(StringUtils.strip(values[i]))
                val converted = converter.readFromString(value)
                result[i] = converted
            }
            return result
        }

        override fun acceptsMultipleValues(): Boolean = true
        override fun valueDescription(): String = Arrays.toString(propertyText)

        @Suppress("UNCHECKED_CAST")
        override fun convertToStringArray(value: Any): Array<String> {
            require(value is Array<*>) { "Type ${value::class.simpleName} is not an array" }
            val length = (value as Array<*>).size
            val result = arrayOfNulls<String>(length)
            for(i in 0..length-1)
                result[i] = "\"${value[i].toString()}\""
            return result as Array<String>
        }

        init {
            propertyText = wrappedProperty.stringList
        }
    }
}