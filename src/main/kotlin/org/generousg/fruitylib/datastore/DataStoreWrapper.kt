package org.generousg.fruitylib.datastore

import com.google.common.collect.Sets
import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamWriter


class DataStoreWrapper<K, V>(localData: Map<K, V>, private val keyWriter: IStreamWriter<K>, private val valueWriter: IStreamWriter<V>, private val keyReader: IStreamReader<K>, private val valueReader: IStreamReader<V>) {
    private val visitors = Sets.newIdentityHashSet<IDataVisitor<K, V>>()
    val localData: DataStore<K, V> = DataStore(localData)
    var activeData: DataStore<K, V>? = null

    private fun notifyVisitors() {
        for(visitor in visitors)
            activeData?.visit(visitor)
    }

    fun activateData(data: DataStore<K, V>) {
        activeData = data
        notifyVisitors()
    }

    fun activateLocalData() {
        activeData = localData
        notifyVisitors()
    }

    fun createReader() = DataStoreReader(this, keyReader, valueReader)
    fun createWriter() = DataStoreWriter(localData, keyWriter, valueWriter)
    fun addVisitor(visitor: IDataVisitor<K, V>) = visitors.add(visitor)
}