package org.generousg.fruitylib.serializable

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

interface IStreamReadable {
    @Throws(IOException::class)
    fun readFromStream(input: DataInput)
}

interface IStreamWritable {
    @Throws(IOException::class)
    fun writeToStream(output: DataOutput)
}