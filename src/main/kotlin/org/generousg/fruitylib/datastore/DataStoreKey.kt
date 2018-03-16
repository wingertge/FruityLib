package org.generousg.fruitylib.datastore


class DataStoreKey<K, V>(val id: String) {
    override fun toString(): String = "<key id = $id, hash = 0x${System.identityHashCode(this)}>"
}