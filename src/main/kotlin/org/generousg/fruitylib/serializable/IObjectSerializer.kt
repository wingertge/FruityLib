package org.generousg.fruitylib.serializable

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException


interface IObjectSerializer<T> {
    @Throws(IOException::class)
    fun readFromStream(obj: T, input: DataInput)
    @Throws(IOException::class)
    fun writeToStream(obj: T, output: DataOutput)
}