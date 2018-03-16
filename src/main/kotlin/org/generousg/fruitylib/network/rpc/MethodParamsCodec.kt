package org.generousg.fruitylib.network.rpc

import com.google.common.base.Preconditions
import com.google.common.reflect.TypeToken
import org.generousg.fruitylib.createFilledArray
import org.generousg.fruitylib.serializable.SerializerRegistry
import org.generousg.fruitylib.util.AnnotationMap
import org.generousg.fruitylib.util.CachedFactory
import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamWriter
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.lang.reflect.Method
import java.lang.reflect.Type


@Suppress("UNUSED_PARAMETER")
class MethodParamsCodec(private val method: Method) {
    companion object {
        private val INSTANCES = object : CachedFactory<Method, MethodParamsCodec>() {
            override fun create(key: Method): MethodParamsCodec {
                return MethodParamsCodec(key)
            }
        }

        @Synchronized fun create(method: Method): MethodParamsCodec {
            return INSTANCES.getOrCreate(method)
        }
    }

    internal class MethodParam(val type: Type, annotations: Array<Annotation>) {
        val isNullable: Boolean
        val serializer = SerializerRegistry.instance.value.findSerializer(type)

        init {
            val annotationsMap = AnnotationMap(annotations)
            isNullable = annotationsMap.hasAnnotation(NullableArg::class)

            requireNotNull(serializer) { "Failed to find serializer for type $type" }
        }

        fun validate() {
            validate(TypeToken.of(type))
        }

        private fun validate(type: TypeToken<*>?) {
            Preconditions.checkState(!type!!.isPrimitive || !isNullable, "Primitive types can't be nullable")
            if (type.isArray) validate(type.componentType)
        }

        override fun toString(): String = "MethodParam [type=$type, nullable=$isNullable]"
    }


    private val params: Array<MethodParam>

    init {
        val annotations = method.parameterAnnotations
        val types = method.parameterTypes

        this.params = createFilledArray(types.size, { MethodParam(types[it], annotations[it]) })
    }

    fun writeArgs(output: DataOutput, vararg args: Any?) {
        Preconditions.checkArgument(args.size == params.size,
                "Argument list length mismatch, expected %d, got %d", params.size, args.size)
        for (i in args.indices) {
            val param = params[i]
            try {
                writeArg(output, i, param.serializer!!, param.isNullable, args[i])
            } catch (e: Exception) {
                throw RuntimeException(String.format("Failed to write argument %d from method %s", i, method), e)
            }
        }
    }

    @Throws(IOException::class)
    private fun writeArg(output: DataOutput, argIndex: Int, writer: IStreamWriter<Any?>, isNullable: Boolean, value: Any?) {
        if (isNullable) {
            if (value == null) {
                output.writeBoolean(false)
                return
            }
            output.writeBoolean(true)
        } else {
            checkNotNull(value, {"Only @NullableArg arguments can be null"})
        }

        writer.writeToStream(value, output)
    }

    fun readArgs(input: DataInput): Array<Any?>? {
        if (params.isEmpty()) return null

        val result = arrayOfNulls<Any>(params.size)
        for (i in params.indices) {
            val param = params[i]
            try {
                result[i] = readArg(input, param.serializer!!, param.isNullable)
            } catch (e: Exception) {
                throw RuntimeException(String.format("Failed to read argument %d from method %s", i, method), e)
            }

        }

        return result
    }

    @Throws(IOException::class)
    private fun readArg(input: DataInput, reader: IStreamReader<Any?>, isNullable: Boolean): Any? {
        if (isNullable) {
            val hasValue = input.readBoolean()
            if (!hasValue) return null
        }

        return reader.readFromStream(input)
    }

    fun validate() {
        for (i in params.indices) {
            try {
                params[i].validate()
            } catch (e: Exception) {
                throw IllegalStateException(String.format("Failed to validate arg %d of method %s", i, method), e)
            }
        }
    }
}