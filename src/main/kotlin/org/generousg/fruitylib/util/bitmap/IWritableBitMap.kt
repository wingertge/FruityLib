package org.generousg.fruitylib.util.bitmap


interface IWriteableBitMap<in T> {

    fun mark(value: T)

    fun clear(value: T)

    operator fun set(key: T, value: Boolean)

    fun toggle(value: T)

    fun clearAll()

}