package org.generousg.fruitylib.serializable.providers

import com.google.common.collect.ImmutableList
import com.google.common.collect.Sets
import com.google.common.io.ByteStreams
import com.google.common.reflect.TypeToken
import org.generousg.fruitylib.reflect.TypeUtils
import org.generousg.fruitylib.serializable.IGenericSerializerProvider
import org.generousg.fruitylib.serializable.SerializerRegistry
import org.generousg.fruitylib.util.ByteUtils
import org.generousg.fruitylib.util.io.IStreamSerializer
import org.generousg.fruitylib.util.io.OutputBitStream
import java.io.DataInput
import java.io.DataOutput
import java.lang.reflect.Type
import java.util.*


class ListSerializerProvider : IGenericSerializerProvider {
    override fun getSerializer(type: Type): IStreamSerializer<*>? {
        val typeToken = TypeToken.of(type)

        if(TypeUtils.LIST_TOKEN.isAssignableFrom(typeToken)) {
            val componentType = typeToken.resolveType(TypeUtils.LIST_VALUE_PARAM)
            return object : NullableCollectionSerializer<MutableList<Any?>>(componentType) {
                override fun createCollection(componentType: TypeToken<*>, length: Int): MutableList<Any?> = Arrays.asList(arrayOfNulls<Any?>(length))
                override fun getLength(collection: MutableList<Any?>): Int = collection.size
                override fun getElement(collection: MutableList<Any?>, index: Int): Any? = collection[index]
                override fun setElement(collection: MutableList<Any?>, index: Int, value: Any?) {
                    collection[index] = value
                }

            }
        }
        return null
    }

}

open class SetSerializerProvider: IGenericSerializerProvider {
    override fun getSerializer(type: Type): IStreamSerializer<*>? {
        val typeToken = TypeToken.of(type)

        if(TypeUtils.SET_TOKEN.isAssignableFrom(typeToken)) {
            val componentType = typeToken.resolveType(TypeUtils.SET_VALUE_PARAM)
            return createSetSerializer(componentType)
        }

        return null
    }

    protected fun createSetSerializer(componentType: TypeToken<*>): IStreamSerializer<Set<*>> {
        val arraySerializer = NullableCollectionSerializer.createObjectArraySerializer(componentType)
        return object : IStreamSerializer<Set<*>> {
            override fun writeToStream(o: Set<*>, output: DataOutput) = arraySerializer.writeToStream(o.toTypedArray(), output)

            override fun readFromStream(input: DataInput): Set<*> {
                val values = arraySerializer.readFromStream(input)
                return Sets.newHashSet(values)
            }
        }
    }
}

class MapSerializerProvider : IGenericSerializerProvider {
    override fun getSerializer(type: Type): IStreamSerializer<*>? {
        val typeToken = TypeToken.of(type)

        if(TypeUtils.MAP_TOKEN.isAssignableFrom(typeToken)) {
            val keyType = typeToken.resolveType(TypeUtils.MAP_KEY_PARAM)
            val valueType = typeToken.resolveType(TypeUtils.MAP_VALUE_PARAM)

            val keySerializer = getSerializer(keyType)
            val valueSerializer = getSerializer(valueType)

            return object : IStreamSerializer<Map<Any?, Any?>> {
                override fun writeToStream(o: Map<Any?, Any?>, output: DataOutput) {
                    val length = o.size
                    ByteUtils.writeVLI(output, length)

                    if(length > 0) {
                        val nullBits = ByteStreams.newDataOutput()
                        val nullBitsStream = OutputBitStream.create(nullBits)

                        val entries = ImmutableList.copyOf(o.entries)

                        entries.forEach {
                            nullBitsStream.writeBit(it.key != null)
                            nullBitsStream.writeBit(it.value != null)
                        }

                        nullBitsStream.flush()
                        output.write(nullBits.toByteArray())

                        entries.forEach {
                            writeValue(it.key, keySerializer, output)
                            writeValue(it.value, valueSerializer, output)
                        }
                    }
                }

                override fun readFromStream(input: DataInput): Map<Any?, Any?> {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                private fun writeValue(value: Any?, serializer: IStreamSerializer<Any?>, output: DataOutput) {
                    if(value != null) serializer.writeToStream(value, output)
                }
            }
        }
        return null
    }

    private fun getSerializer(type: TypeToken<*>): IStreamSerializer<Any?> {
        val keySerializer = SerializerRegistry.instance.value.findSerializer(type.type)
        requireNotNull(keySerializer) { "Can't find serializer for $type" }
        return keySerializer!!
    }
}