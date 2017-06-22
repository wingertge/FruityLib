package org.generousg.fruitylib.util.io

import java.io.DataOutput
import java.io.IOException
import java.io.OutputStream


abstract class OutputBitStream {
    private var buffer = 0
    private var bitCount = 0
    private var byteCount = 0
    val bytesWritten get() = byteCount

    @Throws(IOException::class)
    protected abstract fun writeByte(b: Int)

    @Throws(IOException::class)
    fun writeBit(bit: Boolean) {
        if(bitCount >= 8) flushBuffer()
        buffer = buffer shl 1
        if(bit) buffer = buffer or 1
        bitCount += 1
    }

    @Throws(IOException::class)
    fun flush() { if(bitCount > 0) flushBuffer() }

    @Throws(IOException::class)
    private fun flushBuffer() {
        buffer = buffer shl 8-bitCount
        writeByte(buffer)
        byteCount++
        bitCount = 0
        buffer = 0
    }

    companion object {
        fun create(output: DataOutput) = object : OutputBitStream() {
            override fun writeByte(b: Int) = output.write(b)
        }

        fun create(output: OutputStream) = object : OutputBitStream() {
            override fun writeByte(b: Int) = output.write(b)
        }
    }
}