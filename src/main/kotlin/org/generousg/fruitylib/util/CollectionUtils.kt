package org.generousg.fruitylib.util

import com.google.common.base.Preconditions
import com.google.common.base.Throwables
import com.google.common.reflect.TypeToken
import org.generousg.fruitylib.reflect.TypeUtils
import org.generousg.fruitylib.util.io.IStreamReader
import org.generousg.fruitylib.util.io.IStreamWriter
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.*


@Suppress("unused")
object CollectionUtils {

    val rnd = Random()

    fun <T> getFirst(collection: Collection<T>): T {
        Preconditions.checkArgument(!collection.isEmpty(), "Collection cannot be empty")
        return collection.iterator().next()
    }

    fun <T> getRandom(collection: Collection<T>): T? {
        val size = collection.size
        Preconditions.checkArgument(size > 0, "Can't select from empty collection")
        if (size == 1) return getFirst(collection)
        val randomIndex = rnd.nextInt(size)
        var i = 0
        for (obj in collection) {
            if (i == randomIndex) return obj
            i += 1
        }
        return null
    }

    fun <T> getRandom(list: List<T>): T? {
        val size = list.size
        Preconditions.checkArgument(size > 0, "Can't select from empty list")
        if (size == 0) return null
        if (size == 1) return list[0]
        val randomIndex = rnd.nextInt(list.size)
        return list[randomIndex]
    }

    fun <T> getWeightedRandom(collection: Map<T, Int>): T? {
        var totalWeight = 0
        val values = collection.values
        for (i in values) {
            totalWeight += i
        }

        var r = rnd.nextInt(totalWeight)
        for ((key, value) in collection) {
            r -= value
            if (r <= 0) {
                return key
            }
        }
        return null
    }

    fun readSortedIdList(input: DataInput, output: MutableCollection<Int>) {
        val elemCount = ByteUtils.readVLI(input)

        var currentId = 0
        for (i in 0..elemCount - 1) {
            currentId += ByteUtils.readVLI(input)
            output.add(currentId)
        }
    }

    fun writeSortedIdList(output: DataOutput, idList: SortedSet<Int>) {
        ByteUtils.writeVLI(output, idList.size)

        var currentId = 0
        for (id in idList) {
            val delta = id!! - currentId
            ByteUtils.writeVLI(output, delta)
            currentId = id
        }
    }

    fun <D> readSortedIdMap(input: DataInput, output: MutableMap<Int, D>, reader: IStreamReader<D>) {
        val elemCount = ByteUtils.readVLI(input)

        var currentId = 0
        try {
            for (i in 0..elemCount - 1) {
                currentId += ByteUtils.readVLI(input)
                val data = reader.readFromStream(input)
                output.put(currentId, data)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    fun <D> writeSortedIdMap(output: DataOutput, input: SortedMap<Int, D>, writer: IStreamWriter<D>) {
        ByteUtils.writeVLI(output, input.size)

        var currentId = 0
        try {
            for (e in input.entries) {
                val id = e.key
                val delta = id - currentId
                ByteUtils.writeVLI(output, delta)
                writer.writeToStream(e.value, output)
                currentId = id
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    private fun <A, B> allocateArray(transformer: com.google.common.base.Function<A, B>, length: Int): Any {
        val token = TypeToken.of(transformer.javaClass)
        val typeB = token.resolveType(TypeUtils.FUNCTION_B_PARAM)
        return java.lang.reflect.Array.newInstance(typeB.getRawType(), length)
    }

    @Suppress("UNCHECKED_CAST")
    fun <A, B> transform(input: Array<A>, transformer: com.google.common.base.Function<A, B>): Array<B> {
        val result = allocateArray(transformer, input.size)

        for (i in input.indices) {
            val o = transformer.apply(input[i])
            java.lang.reflect.Array.set(result, i, o)
        }

        return result as Array<B>
    }

    @Suppress("UNCHECKED_CAST")
    fun <A, B> transform(input: Collection<A>, transformer: com.google.common.base.Function<A, B>): Array<B> {
        val result = allocateArray(transformer, input.size)

        input.map { transformer.apply(it) } .forEachIndexed { i, o -> java.lang.reflect.Array.set(result, i, o) }

        return result as Array<B>
    }

    fun <K, V> putOnce(map: MutableMap<K, V>, key: K, value: V) {
        val prev = map.put(key, value)
        Preconditions.checkState(prev == null, "Duplicate value on key %s: %s -> %s", key, prev, value)
    }
}