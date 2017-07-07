package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTUtil
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*


class SyncableUUID(value: UUID = IDENTITY) : SyncableObjectBase(), ISyncableValueProvider<UUID> {
    companion object {
        val IDENTITY = UUID(0, 0)
    }

    private var _value = IDENTITY
    override var value: UUID set(value) {
        if(value != _value) markDirty()
        _value = value
    } get() = _value

    init {
        _value = value
    }

    override fun readFromStream(stream: DataInputStream) {
        val mostSigBits = stream.readLong()
        val leastSigBits = stream.readLong()
        _value = UUID(mostSigBits, leastSigBits)
    }

    override fun writeToStream(stream: DataOutputStream) {
        stream.writeLong(_value.mostSignificantBits)
        stream.writeLong(_value.leastSignificantBits)
    }

    override fun writeToNBT(nbt: NBTTagCompound, name: String) {
        val tag = NBTUtil.createUUIDTag(_value)
        nbt.setTag(name, tag)
    }

    override fun readFromNBT(nbt: NBTTagCompound, name: String) {
        val tag = nbt.getCompoundTag(name)
        _value = NBTUtil.getUUIDFromTag(tag)
    }
}