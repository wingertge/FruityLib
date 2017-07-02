package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import java.io.DataInputStream
import java.io.DataOutputStream


@Suppress("DEPRECATION")
class SyncableCoordList : SyncableObjectBase(), ISyncableValueProvider<List<BlockPos>>, Iterable<BlockPos> {
    @Deprecated("DO NOT USE!!! WILL NOT WORK!!!")
    override var value = mutableListOf<BlockPos>()
    val size get() = value.size

    operator fun get(id: Int) = value[id]
    fun add(newValue: BlockPos) {
        value.add(newValue)
        markDirty()
    }

    fun remove(newValue: BlockPos) {
        value.remove(newValue)
        markDirty()
    }

    fun clear() {
        value.clear()
        markDirty()
    }

    fun addAll(collection: Iterable<BlockPos>) {
        value.addAll(collection)
        markDirty()
    }

    override fun iterator() = value.iterator()

    override fun readFromStream(stream: DataInputStream) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeToStream(stream: DataOutputStream) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeToNBT(nbt: NBTTagCompound, name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun readFromNBT(nbt: NBTTagCompound, name: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}