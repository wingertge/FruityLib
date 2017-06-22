package org.generousg.fruitylib.datastore

import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamWriter
import util.ByteUtils
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException


class DataStore<K, V>(private val values: Map<K, V>) {
    operator fun get(key: K) = values[key]
    fun visit(visitor: IDataVisitor<K, V>) {
        visitor.begin(values.size)
        for((key, value) in values.entries)
            visitor.entry(key, value)

        visitor.end()
    }
}

class DataStoreReader<K, V>(private val wrapper: DataStoreWrapper<K, V>, private val keyReader: IStreamReader<K>, private val valueReader: IStreamReader<V>) {
    fun read(input: DataInput) {
        val size = ByteUtils.readVLI(input)
        val values = hashMapOf<K, V>()

        try {
            for(i in 0..size-1) {
                val key = keyReader.readFromStream(input)
                val value = valueReader.readFromStream(input)
                values.put(key, value)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val result = DataStore(values)
        wrapper.activateData(result)
    }
}

class DataStoreWriter<K, V>(private val data: DataStore<K, V>, private val keyWriter: IStreamWriter<K>, private val valueWriter: IStreamWriter<V>) {
    fun write(output: DataOutput) {
        data.visit(object: IDataVisitor<K, V> {
            override fun begin(size: Int) = ByteUtils.writeVLI(output, size)

            override fun entry(key: K, value: V) {
                try {
                    keyWriter.writeToStream(key, output)
                    valueWriter.writeToStream(value, output)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }

            override fun end() = Unit
        })
    }
}