package org.genguava.fruitylib.util

import java.io.DataInput
import java.io.EOFException
import java.io.IOException
import java.io.InputStream


class StreamUtils {
    class EndofStreamException : RuntimeException()

    companion object {
        fun bitsToBytes(bits: Int) = (bits + 7) / 8
        @Throws(IOException::class)
        fun readBytes(stream: DataInput, count: Int): ByteArray {
            val buffer = ByteArray(count)
            try {
                stream.readFully(buffer)
            } catch (e: EOFException) {
                throw EndofStreamException()
            }

            return buffer
        }

        fun readBytes(stream: InputStream, count: Int): ByteArray {
            val buffer = ByteArray(count)
            val read = stream.read(buffer)
            if(read != count) throw EndofStreamException()
            return buffer
        }
    }
}