package org.generousg.fruitylib.util

import com.google.common.collect.ImmutableList
import com.google.common.primitives.Ints
import java.lang.reflect.Field
import java.util.*


abstract class FieldsSelector {
    class FieldEntry(val field: Field, val rank: Int) : Comparable<FieldEntry> {
        override fun compareTo(other: FieldEntry): Int {
            val result = Ints.compare(rank, other.rank)
            return if(result != 0 ) result else field.name.compareTo(other.field.name)
        }
    }

    val cache = object : CachedFactory<Class<*>, Collection<Field>>() {
        override fun create(key: Class<*>): Collection<Field> = scanForFields(key)
    }

    protected abstract fun listFields(cls: Class<*>): List<FieldEntry>

    private fun scanForFields(cls: Class<*>): Collection<Field> {
        val entries = listFields(cls)
        Collections.sort(entries)

        val result = ImmutableList.builder<Field>()
        for(entry in entries) {
            result.add(entry.field)
            entry.field.isAccessible = true
        }
        return result.build()
    }

    fun getFields(cls: Class<*>): Collection<Field> = synchronized(cache) { return cache.getOrCreate(cls) }
}