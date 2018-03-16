package org.generousg.fruitylib.util


abstract class CachedFactory<K, V> {
    private val cache = hashMapOf<K, V>()

    fun getOrCreate(key: K): V {
        var value = cache[key]

        if(value == null) {
            value = create(key)
            cache.put(key, value)
        }

        return value!!
    }

    fun remove(key: K) = cache.remove(key)
    protected abstract fun create(key: K): V
}