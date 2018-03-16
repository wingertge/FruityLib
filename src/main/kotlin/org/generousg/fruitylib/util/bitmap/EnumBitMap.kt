package org.generousg.fruitylib.util.bitmap

import org.generousg.fruitylib.util.ByteUtils


class EnumBitMap<in T : Enum<*>>(values: Iterable<T>) {
    private var value = 0

    init {
        for(enumVal in values) value = ByteUtils.on(value, enumVal.ordinal)
    }

    fun contains(enumVal: T) = ByteUtils.get(value, enumVal.ordinal)
}