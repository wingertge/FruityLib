package org.generousg.fruitylib.datastore

import com.google.common.collect.HashBiMap
import com.google.common.collect.Sets


open class DataStoreManager {
    class UnknownKeyException(key: DataStoreKey<*, *>) : RuntimeException(key.toString())
    class UnknownKeyIdException(id: String) : RuntimeException(id)

    private val dataStoreKeys = HashBiMap.create<DataStoreKey<*, *>, String>()
    protected val dataStoreMeta = hashMapOf<DataStoreKey<*, *>, DataStoreWrapper<*, *>>()

    private fun <K, V> checkKeyExists(key: DataStoreKey<K, V>) {
        if(!dataStoreKeys.containsKey(key)) throw UnknownKeyException(key)
    }

    protected fun <K, V> getDataStoreMeta(key: DataStoreKey<K, V>): DataStoreWrapper<K, V> {
        checkKeyExists(key)

        @Suppress("UNCHECKED_CAST")
        val meta = dataStoreMeta[key] as? DataStoreWrapper<K, V> ?: throw UnknownKeyException(key)
        return meta
    }

    protected fun <V, K> getDataStoreMeta(id: String): DataStoreWrapper<out K, out V> {
        @Suppress("UNCHECKED_CAST")
        val key = dataStoreKeys.inverse()[id] as? DataStoreKey<out K, out V> ?: throw UnknownKeyIdException(id)
        return getDataStoreMeta(key)
    }

    fun <K, V> getLocalDataStore(key: DataStoreKey<K, V>) = getDataStoreMeta(key).localData
    fun <K, V> getActiveDataStore(key: DataStoreKey<K, V>) = getDataStoreMeta(key).activeData

    fun <K, V> createDataStoreReader(id: String) = getDataStoreMeta<V, K>(id).createReader()
    fun <K, V> createDataStoreWriter(key: DataStoreKey<K, V>) = getDataStoreMeta(key).createWriter()

    fun <K, V> addCallback(key: DataStoreKey<K, V>, visitor: IDataVisitor<K, V>) = getDataStoreMeta(key).addVisitor(visitor)

    fun activateLocalData() = dataStoreMeta.values.forEach { it.activateLocalData() }

    open fun <K, V> createDataStore(id: String, keyClass: Class<out K>, valueClass: Class<out V>): DataStoreBuilder<K, V> {
        val key = DataStoreKey<K, V>(id)
        val prev = dataStoreKeys.put(key, id)
        require(prev == null) { "Overwriting key with name $id" }
        return DataStoreBuilder(this, key, keyClass, valueClass)
    }

    internal fun <K, V> register(key: DataStoreKey<K, V>, meta: DataStoreWrapper<K, V>) {
        checkKeyExists(key)
        val prev = dataStoreMeta.put(key, meta)
        require(prev == null) { "Overwriting wrapper for key $key" }
    }

    fun validate() {
        val missing = Sets.difference(dataStoreKeys.keys, dataStoreMeta.keys)
        require(missing.isEmpty()) { "Keys [$missing] were registered, but are not associated with any data" }
    }
}