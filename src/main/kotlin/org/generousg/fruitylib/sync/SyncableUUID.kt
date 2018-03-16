package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTUtil
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import kotlin.reflect.KProperty


class SyncableUUID(value: UUID = IDENTITY) : SyncableObjectBase(), ISyncableValueProvider<UUID> {
    override var value: UUID get() = _value
    set(value) {
        if(value != _value) markDirty()
        _value = value
    }

    companion object {
        val IDENTITY = UUID(0, 0)
    }

    private var _value = IDENTITY

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

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): UUID {
        return value
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: UUID) {
        this.value = value
    }
}
