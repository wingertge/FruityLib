package org.generousg.fruitylib.serializable

import com.google.common.base.Preconditions
import com.google.common.collect.Maps
import org.generousg.fruitylib.reflect.ConstructorAccess
import org.generousg.fruitylib.reflect.TypeUtils
import org.generousg.fruitylib.serializable.providers.*
import org.generousg.fruitylib.util.TypeRW
import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamSerializer
import org.generousg.fruitylib.util.io.IStreamWriter
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.lang.reflect.Type


@Suppress("UNUSED_PARAMETER", "unused")
open class SerializerRegistry {
    object InstanceHolder {
        val INSTANCE = SerializerRegistry()
    }
    companion object {
        val instance = lazy { InstanceHolder.INSTANCE }
    }

    private val serializers = Maps.newHashMap<Class<*>, IStreamSerializer<*>>(TypeRW.STREAM_SERIALIZERS)
    private val providers = arrayListOf<ISerializerProvider>()
    private val genericProviders = arrayListOf<IGenericSerializerProvider>()

    init {
        providers.add(EnumSerializerProvider())
        providers.add(ArraySerializerProvider())
        providers.add(ClassSerializerProvider())

        genericProviders.add(ListSerializerProvider())
        genericProviders.add(SetSerializerProvider())
        genericProviders.add(MapSerializerProvider())
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> resolve(intf: Class<*>, concrete: Class<*>): Class<out T> {
        val rawType = TypeUtils.getTypeParameter(intf, concrete).rawType
        return rawType as Class<out T>
    }

    fun <T> register(target: Class<out T>, serializer: IStreamSerializer<T>) {
        Preconditions.checkArgument(target != Any::class.java, "Can't register serializer for Object")
        val prev = serializers.put(target, serializer)
        Preconditions.checkState(prev == null, "Duplicate serializer for %s", target)
    }

    fun <T> register(serializer: IStreamSerializer<T>) {
        val cls = resolve<T>(IStreamSerializer::class.java, serializer.javaClass)
        register(cls, serializer)
    }

    fun <T> registerSerializable(cls: Class<out T>, factory: IInstanceFactory<T>) where T : IStreamWritable, T : IStreamReadable {
        register(cls, SerializerAdapters.createFromFactory(factory))
    }

    fun <T> registerSerializable(factory: IInstanceFactory<T>) where T : IStreamWritable, T : IStreamReadable {
        val cls = resolve<T>(IInstanceFactory::class.java, factory.javaClass)
        registerSerializable(cls, factory)
    }

    fun <T> registerSerializable(cls: Class<T>) where T : IStreamWritable, T : IStreamReadable {
        val factory = ConstructorAccess.create(cls)
        registerSerializable(cls, factory)
    }

    fun <T : IStreamWritable> registerWriteable(cls: Class<out T>, reader: IStreamReader<T>) {
        register(cls, SerializerAdapters.createFromReader(reader))
    }

    fun <T : IStreamWritable> registerWriteable(reader: IStreamReader<T>) {
        val cls = resolve<T>(IStreamReader::class.java, reader.javaClass)
        registerWriteable(cls, reader)
    }

    fun registerProvider(provider: ISerializerProvider) {
        Preconditions.checkNotNull(provider)
        providers.add(provider)
    }

    private fun findClassSerializer(cls: Class<*>, serializer: IStreamSerializer<*>?): IStreamSerializer<*>? {
        var serial: IStreamSerializer<*>?
        for (provider in providers) {
            serial = provider.getSerializer(cls)
            if (serial != null) {
                serializers.put(cls, serial)
                return serial
            }
        }

        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> findSerializer(cls: Class<out T>): IStreamSerializer<T> {
        var serializer: IStreamSerializer<*>? = serializers[cls]

        if (serializer == null) serializer = findClassSerializer(cls, serializer)

        return serializer as IStreamSerializer<T>
    }

    fun findSerializer(type: Type): IStreamSerializer<Any?>? {
        if (type is Class<*>) return findSerializer(type)
        return findGenericSerializer(type)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun findGenericSerializer(type: Type): IStreamSerializer<Any?>? {
        genericProviders.mapNotNull { it.getSerializer(type) } .forEach { return it as IStreamSerializer<Any?> }

        return null
    }

    @Throws(IOException::class)
    fun <T> createFromStream(input: DataInput, cls: Class<out T>): T {
        val reader = findSerializer(cls)
        Preconditions.checkNotNull<IStreamReader<T>>(reader, "Can't find reader for %s", cls)
        return reader.readFromStream(input)
    }

    @Throws(IOException::class)
    fun createFromStream(input: DataInput, type: Type): Any? {
        val reader = findSerializer(type)
        Preconditions.checkNotNull<IStreamReader<*>>(reader, "Can't find reader for %s", type)
        return reader?.readFromStream(input)
    }

    @Throws(IOException::class)
    fun <T> writeToStream(output: DataOutput, cls: Class<out T>, target: T) {
        Preconditions.checkNotNull(target)

        val writer = findSerializer(cls)
        Preconditions.checkNotNull<IStreamWriter<T>>(writer, "Can't find writer for %s", cls)
        writer.writeToStream(target, output)
    }

    @Throws(IOException::class)
    fun writeToStream(output: DataOutput, type: Type, target: Any) {
        Preconditions.checkNotNull(target)

        val writer = findSerializer(type)
        Preconditions.checkNotNull<IStreamWriter<Any>>(writer, "Can't find writer for %s", type)
        writer?.writeToStream(target, output)
    }
}