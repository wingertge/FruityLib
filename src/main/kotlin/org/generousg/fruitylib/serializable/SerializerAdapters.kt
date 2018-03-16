package org.generousg.fruitylib.serializable

import org.generousg.fruitylib.reflect.ConstructorAccess
import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamSerializer
import java.io.DataInput
import java.io.DataOutput


class SerializerAdapters {
    companion object {
        fun <T> createFromFactory(factory: IInstanceFactory<T>): IStreamSerializer<T> where T : IStreamReadable, T:IStreamWritable = object : IStreamSerializer<T> {
            override fun readFromStream(input: DataInput): T {
                val instance = factory.create()
                instance.readFromStream(input)
                return instance
            }

            override fun writeToStream(o: T, output: DataOutput) {
                o.writeToStream(output)
            }
        }
        fun <T: IStreamWritable> createFromReader(reader: IStreamReader<T>): IStreamSerializer<T> = object : IStreamSerializer<T> {
            override fun readFromStream(input: DataInput): T = reader.readFromStream(input)
            override fun writeToStream(o: T, output: DataOutput) = o.writeToStream(output)
        }
        fun <T> createFromObjectSerializer(factory: IInstanceFactory<T>, serializer: IObjectSerializer<T>): IStreamSerializer<T> = object : IStreamSerializer<T> {
            override fun readFromStream(input: DataInput): T {
                val obj = factory.create()
                serializer.readFromStream(obj, input)
                return obj
            }

            override fun writeToStream(o: T, output: DataOutput) = serializer.writeToStream(o, output)
        }
        fun <T> createFromObjectSerializer(cls: Class<out T>, serializer: IObjectSerializer<T>): IStreamSerializer<T> {
            val factory = ConstructorAccess.create(cls)
            return createFromObjectSerializer(factory, serializer)
        }
    }
}