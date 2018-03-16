package org.genguava.fruitylib.util.io

import java.io.ByteArrayInputStream
import java.io.DataInput
import java.io.IOException
import java.io.InputStream


abstract class InputBitStream {
    protected abstract fun nextByte(): Int
    private var byteCounter = 0
    private var mask = 0
    private var currentByte = 0
    val bytesRead get() = byteCounter

    @Throws(IOException::class)
    fun readBit(): Boolean {
        if(mask == 0) {
            currentByte = nextByte()
            byteCounter++
            mask = 0x80 + Int.MIN_VALUE
        }

        val bit = (currentByte and mask) != 0
        mask = mask shr 1

        return bit
    }

    companion object {
        fun create(input: DataInput): InputBitStream = object: InputBitStream() {
            override fun nextByte(): Int = input.readByte().toInt()
        }

        fun create(input: InputStream): InputBitStream = object: InputBitStream() {
            override fun nextByte(): Int = input.read()
        }

        fun create(bytes: ByteArray) = create(ByteArrayInputStream(bytes))
    }
}