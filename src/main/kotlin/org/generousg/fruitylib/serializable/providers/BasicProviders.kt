package org.generousg.fruitylib.serializable.providers

import com.google.common.reflect.TypeToken
import org.generousg.fruitylib.serializable.ISerializerProvider
import org.generousg.fruitylib.serializable.SerializerAdapters
import org.generousg.fruitylib.serializable.SerializerRegistry
import org.generousg.fruitylib.serializable.cls.ClassSerializersProvider
import org.generousg.fruitylib.serializable.cls.SerializableClass
import org.generousg.fruitylib.util.ByteUtils
import org.generousg.fruitylib.util.io.IStreamSerializer
import java.io.DataInput
import java.io.DataOutput
import java.lang.reflect.Array


class EnumSerializerProvider : ISerializerProvider {
    override fun getSerializer(cls: Class<*>): IStreamSerializer<*>? {
        return if(Enum::class.java.isAssignableFrom(cls)) createSerializer(cls) else null
    }

    private fun createSerializer(cls: Class<*>): IStreamSerializer<*>? {
        val superCls = cls.superclass
        val values = if(superCls == Enum::class.java) cls.enumConstants else superCls.enumConstants

        return object: IStreamSerializer<Any?> {
            override fun writeToStream(o: Any?, output: DataOutput) {
                val ord = (o as Enum<*>).ordinal
                ByteUtils.writeVLI(output, ord)
            }

            override fun readFromStream(input: DataInput): Any? {
                val ord = ByteUtils.readVLI(input)

                try {
                    return values[ord]
                } catch (e: ArrayIndexOutOfBoundsException) {
                    throw ArrayIndexOutOfBoundsException("Failed to get enum with ordinal $ord from class $cls")
                }
            }
        }
    }
}
class ArraySerializerProvider : ISerializerProvider {
    override fun getSerializer(cls: Class<*>): IStreamSerializer<*>? {
        if(cls.isArray) {
            val componentCls = TypeToken.of(cls).componentType
            return if(componentCls!!.isPrimitive) createPrimitiveSerializer(componentCls) else createNullableSerializer(componentCls)
        }
        return null
    }

    private fun createPrimitiveSerializer(componentType: TypeToken<*>): IStreamSerializer<Any?> {
        val componentSerializer = SerializerRegistry.instance.value.findSerializer(componentType.type)
        val componentCls = componentType.rawType

        return object: IStreamSerializer<Any?> {
            override fun writeToStream(o: Any?, output: DataOutput) {
                val length = Array.getLength(o)
                ByteUtils.writeVLI(output, length)

                (0..length-1)
                        .map { Array.get(o, it) }
                        .forEach { componentSerializer?.writeToStream(it, output) }
            }

            override fun readFromStream(input: DataInput): Any? {
                val length = ByteUtils.readVLI(input)
                val result = Array.newInstance(componentCls, length)

                (0..length-1).forEach {
                    val value = componentSerializer!!.readFromStream(input)
                    Array.set(result, it, value)
                }

                return result
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun createNullableSerializer(componentType: TypeToken<*>): IStreamSerializer<*> = object: NullableCollectionSerializer<Any?>(componentType) {
        override fun createCollection(componentType: TypeToken<*>, length: Int): Any? = Array.newInstance(componentType.rawType, length)
        override fun getLength(collection: Any?): Int = Array.getLength(collection)
        override fun getElement(collection: Any?, index: Int): Any? = Array.get(collection, index)
        override fun setElement(collection: Any?, index: Int, value: Any?) = Array.set(collection, index, value)
    }
}
class ClassSerializerProvider: ISerializerProvider {
    override fun getSerializer(cls: Class<*>): IStreamSerializer<*>? {
        if(cls.isAnnotationPresent(SerializableClass::class.java)) {
            val objectSerializer = ClassSerializersProvider.instance.value.getSerializer(cls)
            return SerializerAdapters.createFromObjectSerializer(cls, objectSerializer)
        }
        return null
    }

}
