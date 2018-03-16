package org.generousg.fruitylib.serializable.cls

import com.google.common.collect.ImmutableList
import com.google.common.io.ByteStreams
import org.generousg.fruitylib.reflect.FieldAccess
import org.generousg.fruitylib.reflect.TypeUtils
import org.generousg.fruitylib.serializable.IObjectSerializer
import org.generousg.fruitylib.serializable.SerializerRegistry
import org.generousg.fruitylib.util.StreamUtils
import org.generousg.fruitylib.util.io.IStreamSerializer
import org.generousg.fruitylib.util.io.InputBitStream
import org.generousg.fruitylib.util.io.OutputBitStream
import java.io.DataInput
import java.io.DataOutput
import java.lang.reflect.Field


class ClassSerializerBuilder<T>(private val ownerClass: Class<out T>) {
    internal class SerializableField(ownerClass: Class<*>, field: Field, internal val isNullable: Boolean) : FieldAccess<Any?>(field) {
        val serializer: IStreamSerializer<Any?>

        init {
            val fieldType = TypeUtils.resolveFieldType(ownerClass, field)
            serializer = SerializerRegistry.instance.value.findSerializer(fieldType?.rawType!!)
            requireNotNull(serializer) { "Invalid field $field type" }
        }
    }
    internal class NonNullableSerializer<T>(private val fields: List<SerializableField>) : IObjectSerializer<T> {
        override fun readFromStream(obj: T, input: DataInput) {
            for(field in fields) {
                val value = field.serializer.readFromStream(input)
                field[obj] = value
            }
        }

        override fun writeToStream(obj: T, output: DataOutput) {
            for(field in fields) {
                val value = field[obj]
                requireNotNull(value) { "Non-nullable ${field.field} has null value" }
                field.serializer.writeToStream(value, output)
            }
        }
    }
    internal class NullableSerializer<T>(fields0: List<SerializableField>, private val nullBytesCount: Int) : IObjectSerializer<T> {
        private val fields = ImmutableList.copyOf(fields0)

        override fun readFromStream(obj: T, input: DataInput) {
            val nullBits = StreamUtils.readBytes(input, nullBytesCount)
            val nullBitStream = InputBitStream.create(nullBits)

            for(field in fields) {
                val isNull = field.isNullable && nullBitStream.readBit()
                val value = if(isNull) null else field.serializer.readFromStream(input)
                field[obj] = value
            }
        }

        override fun writeToStream(obj: T, output: DataOutput) {
            val payload = ByteStreams.newDataOutput()
            val nullBitsStream = OutputBitStream.create(output)

            for(field in fields) {
                val value = field[obj]
                if(field.isNullable) {
                    if(value == null)
                        nullBitsStream.writeBit(true)
                    else {
                        nullBitsStream.writeBit(false)
                        field.serializer.writeToStream(value, payload)
                    }
                } else field.serializer.writeToStream(value, payload)
            }

            nullBitsStream.flush()
            output.write(payload.toByteArray())
        }
    }

    private val fields = arrayListOf<SerializableField>()
    private val addedFields = hashSetOf<Field>()
    private var nullableCount = 0

    fun appendField(field: Field) {
        require(field.declaringClass.isAssignableFrom(ownerClass)) { "$field does not belong to $ownerClass" }
        val newlyAdded = addedFields.add(field)
        require(newlyAdded) { "$field already added" }
        val annotation = field.getAnnotation(Serialize::class.java)
        val isNullable = !field.type.isPrimitive && (annotation != null && annotation.nullable)

        if(isNullable) nullableCount++

        fields.add(SerializableField(ownerClass, field, isNullable))
    }

    fun create(): IObjectSerializer<T> = if(nullableCount != 0) NullableSerializer<T>(fields, StreamUtils.bitsToBytes(nullableCount)) else NonNullableSerializer<T>(fields)
}