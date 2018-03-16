package org.generousg.fruitylib.datastore


interface IDataVisitor<K, V> {
    fun begin(size: Int)
    fun entry(key: K, value: V)
    fun end()
}