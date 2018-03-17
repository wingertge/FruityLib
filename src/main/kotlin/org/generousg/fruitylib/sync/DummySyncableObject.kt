package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import org.generousg.fruitylib.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream


class DummySyncableObject : SyncableObjectBase() {

    override fun readFromStream(stream: DataInputStream) = Log.warn {"Trying to read dummy syncable object"}
    override fun writeToStream(stream: DataOutputStream) = Log.warn {"Trying to write dummy syncable object"}
    override fun writeToNBT(nbt: NBTTagCompound, name: String) = Log.warn {"Trying to write dummy syncable object"}
    override fun readFromNBT(nbt: NBTTagCompound, name: String) = Log.warn {"Trying to read dummy syncable object"}

    companion object {
        val instance = lazy { DummySyncableObject() }
    }
}