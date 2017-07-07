package org.generousg.fruitylib.serializable.cls

import org.generousg.fruitylib.serializable.IObjectSerializer
import org.generousg.fruitylib.util.CachedFactory
import org.generousg.fruitylib.util.FieldsSelector
import java.io.DataInput
import java.io.DataOutput


@Suppress("UNUSED_PARAMETER")
class ClassSerializersProvider {
    private object InstanceHolder {
        val INSTANCE = ClassSerializersProvider()
    }
    companion object {
        val instance = lazy { InstanceHolder.INSTANCE }
    }

    private val SELECTOR = object : FieldsSelector() {
        override fun listFields(cls: Class<*>): List<FieldEntry> {
            val result = arrayListOf<FieldEntry>()
            cls.fields.forEach { field ->
                val ann = field.getAnnotation(Serialize::class.java)
                if(ann != null) result.add(FieldEntry(field, ann.rank))
            }

            return result
        }
    }

    private val cache = object : CachedFactory<Class<*>, IObjectSerializer<*>>() {
        override fun create(key: Class<*>): IObjectSerializer<*> {
            val builder = ClassSerializerBuilder<Any?>(key)

            for (field in SELECTOR.getFields(key))
                builder.appendField(field)

            return builder.create()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getSerializer(cls: Class<out T>): IObjectSerializer<T> = cache.getOrCreate(cls) as IObjectSerializer<T>
    inline fun <reified T : Any?> getSerializer(obj: T): IObjectSerializer<T> = getSerializer(T::class.java)

    fun readFromStream(obj: Any?, input: DataInput) = getSerializer(obj).readFromStream(obj, input)
    fun writeToStream(obj: Any?, output: DataOutput) = getSerializer(obj).writeToStream(obj, output)
}