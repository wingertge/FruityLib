package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import kotlin.reflect.KProperty


class SyncableInt : SyncableObjectBase, ISyncableValueProvider<Int> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        this.value = value
    }

    private var _value = 0
    override var value: Int set(nValue) {
        if(nValue != _value) markDirty()
        _value = nValue
    } get() = _value

    constructor(value: Int) {
        this.value = value
    }

    constructor() {}

    @Throws(IOException::class)
    override fun readFromStream(stream: DataInputStream) {
        _value = stream.readInt()
    }

    fun modify(by: Int) {
        set(value + by)
    }

    fun set(`val`: Int) {
        if (`val` != value) {
            _value = `val`
            markDirty()
        }
    }

    fun get(): Int {
        return value
    }

    @Throws(IOException::class)
    override fun writeToStream(stream: DataOutputStream) {
        stream.writeInt(value)
    }

    override fun writeToNBT(nbt: NBTTagCompound, name: String) {
        nbt.setInteger(name, value)
    }

    override fun readFromNBT(nbt: NBTTagCompound, name: String) {
        if (nbt.hasKey(name)) {
            _value = nbt.getInteger(name)
        }
    }
}
