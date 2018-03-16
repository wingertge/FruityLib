package org.genguava.fruitylib.util.io


interface IStreamReader<out T> {
    @Throws(java.io.IOException::class)
    fun readFromStream(input: java.io.DataInput): T
}