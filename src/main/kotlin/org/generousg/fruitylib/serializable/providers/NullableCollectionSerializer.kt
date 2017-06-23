package org.generousg.fruitylib.serializable.providers

import com.google.common.io.ByteStreams
import com.google.common.reflect.TypeToken
import org.generousg.fruitylib.serializable.SerializerRegistry
import org.generousg.fruitylib.util.ByteUtils
import org.generousg.fruitylib.util.StreamUtils
import org.generousg.fruitylib.util.io.IStreamSerializer
import org.generousg.fruitylib.util.io.InputBitStream
import org.generousg.fruitylib.util.io.OutputBitStream
import java.io.DataInput
import java.io.DataOutput


abstract class NullableCollectionSerializer<T>(private val componentType: TypeToken<*>) : IStreamSerializer<T> {
    companion object {
        fun createObjectArraySerializer(componentType: TypeToken<*>): NullableCollectionSerializer<Array<Any?>> {
            return object: NullableCollectionSerializer<Array<Any?>>(componentType) {
                override fun createCollection(componentType: TypeToken<*>, length: Int) = arrayOfNulls<Any?>(length)
                override fun getLength(collection: Array<Any?>) = collection.size
                override fun getElement(collection: Array<Any?>, index: Int) = collection[index]
                override fun setElement(collection: Array<Any?>, index: Int, value: Any?) { collection[index] = value }
            }
        }
    }

    private val componentSerializer: IStreamSerializer<Any?>?

    init {
        val type = componentType.type
        componentSerializer = SerializerRegistry.instance.value.findSerializer(type)
        requireNotNull(componentSerializer) { "Can't find serializer for $type" }
    }

    override fun readFromStream(input: DataInput): T {
        val length = ByteUtils.readVLI(input)
        val result = createCollection(componentType, length)
        if(length > 0) {
            val nullBitsSize = StreamUtils.bitsToBytes(length)
            val nullBits = StreamUtils.readBytes(input, nullBitsSize)
            val nullBitStream = InputBitStream.create(nullBits)

            (0..length-1).forEach {
                if(nullBitStream.readBit()) {
                    val value = componentSerializer?.readFromStream(input)
                    setElement(result, it, value)
                }
            }
        }

        return result
    }

    override fun writeToStream(o: T, output: DataOutput) {
        val length = getLength(o)
        ByteUtils.writeVLI(output, length)

        if(length > 0) {
            val nullBits = ByteStreams.newDataOutput()
            val nullBitsStream = OutputBitStream.create(nullBits)

            (0..length-1).map { getElement(o, it) != null }.forEach { nullBitsStream.writeBit(it) }

            nullBitsStream.flush()
            output.write(nullBits.toByteArray())

            (0..length-1).map { getElement(o, it) }.filter { it != null }.forEach { componentSerializer?.writeToStream(it, output) }
        }
    }

    protected abstract fun createCollection(componentType: TypeToken<*>, length: Int): T
    protected abstract fun getLength(collection: T): Int
    protected abstract fun getElement(collection: T, index: Int): Any?
    protected abstract fun setElement(collection: T, index: Int, value: Any?)
}