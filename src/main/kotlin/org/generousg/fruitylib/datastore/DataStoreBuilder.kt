package org.generousg.fruitylib.datastore

import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamSerializer
import org.generousg.fruitylib.util.io.IStreamWriter
import util.TypeRW


class DataStoreBuilder<K, V>(private val owner: DataStoreManager, private val key: DataStoreKey<K, V>, private val keyClass: Class<out K>, private val valueClass: Class<out V>) {
    var keyWriter: IStreamWriter<K>? = null
    var valueWriter: IStreamWriter<V>? = null
    var keyReader: IStreamReader<K>? = null
    var valueReader: IStreamReader<V>? = null

    private val visitors = arrayListOf<IDataVisitor<K, V>>()
    private val values = hashMapOf<K, V>()

    fun register(): DataStoreKey<K, V> {
        requireNotNull(keyWriter) { "Key writer not set" }
        requireNotNull(valueWriter) { "Value writer not set" }
        requireNotNull(keyReader) { "Key reader not set" }
        requireNotNull(valueReader) { "Value reader not set" }

        val wrapper = DataStoreWrapper<K,V>(values, keyWriter as IStreamWriter<K>, valueWriter as IStreamWriter<V>, keyReader as IStreamReader<K>, valueReader as IStreamReader<V>)

        for(visitor in visitors)
            wrapper.addVisitor(visitor)

        wrapper.activateLocalData()
        owner.register(key, wrapper)

        return key
    }

    fun isRegistered(key: K) = values.containsKey(key)

    fun addEntry(key: K, value: V) {
        val prev = values.put(key, value)
        require(prev == null) { "Replacing value for key $key: $prev -> $value, id: ${this.key.id}" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getDefaultReaderWriter(cls: Class<out T>): TypeRW<T> {
        val rw = TypeRW.UNIVERSAL_SERIALIZERS[cls] as TypeRW<T>
        requireNotNull(rw) { "Can't find default reader/writer for class $cls, id ${key.id}" }
        return rw
    }

    fun setDefaultKeyWriter() { keyWriter = getDefaultReaderWriter(keyClass) }
    fun setDefaultValueWriter() { valueWriter = getDefaultReaderWriter(valueClass) }
    fun setDefaultKeyReader() { keyReader = getDefaultReaderWriter(keyClass) }
    fun setDefaultValueReader() { valueReader = getDefaultReaderWriter(valueClass) }
    fun setDefaultKeyReaderWriter() {
        setDefaultKeyReader()
        setDefaultKeyWriter()
    }
    fun setDefaultValueReaderWriter() {
        setDefaultValueReader()
        setDefaultValueWriter()
    }
    fun setDefaultReadersWriter() {
        setDefaultKeyReaderWriter()
        setDefaultValueReaderWriter()
    }

    fun setKeyReaderWriter(rw: IStreamSerializer<K>) {
        keyReader = rw
        keyWriter = rw
    }
    fun setValueReaderWriter(rw: IStreamSerializer<V>) {
        valueReader = rw
        valueWriter = rw
    }

    fun addVisitor(visitor: IDataVisitor<K, V>) = visitors.add(visitor)
}