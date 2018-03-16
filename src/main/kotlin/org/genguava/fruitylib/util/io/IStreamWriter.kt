package org.genguava.fruitylib.util.io


interface IStreamWriter<in T> {
    @Throws(java.io.IOException::class)
    fun writeToStream(o: T, output: java.io.DataOutput)
}