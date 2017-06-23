package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import kotlin.properties.Delegates


class SyncableInt : SyncableObjectBase, ISyncableValueProvider<Int> {
    override var value: Int by Delegates.observable(0, { _, oldValue, newValue -> if(oldValue != newValue) markDirty() })

    constructor(value: Int) {
        this.value = value
    }

    constructor() {}

    @Throws(IOException::class)
    override fun readFromStream(stream: DataInputStream) {
        value = stream.readInt()
    }

    fun modify(by: Int) {
        set(value + by)
    }

    fun set(`val`: Int) {
        if (`val` != value) {
            value = `val`
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
            value = nbt.getInteger(name)
        }
    }
}