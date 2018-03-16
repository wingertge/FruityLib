package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import java.io.DataInputStream
import java.io.DataOutputStream


interface ISyncableObject {
    fun isDirty(): Boolean
    fun markClean()
    fun markDirty()
    fun readFromStream(stream: DataInputStream)
    fun writeToStream(stream: DataOutputStream)
    fun writeToNBT(nbt: NBTTagCompound, name: String)
    fun readFromNBT(nbt: NBTTagCompound, name: String)
}