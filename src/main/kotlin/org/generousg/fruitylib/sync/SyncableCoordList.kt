package org.generousg.fruitylib.sync

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTUtil
import net.minecraft.util.math.BlockPos
import org.generousg.fruitylib.util.ByteUtils
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
        val size = ByteUtils.readVLI(stream)
        val result = mutableListOf<BlockPos>()
        for(i in 0..size-1) {
            val x = stream.readInt()
            val y = stream.readInt()
            val z = stream.readInt()
            val pos = BlockPos(x, y, z)
            result.add(pos)
        }
        value = result
    }

    override fun writeToStream(stream: DataOutputStream) {
        ByteUtils.writeVLI(stream, value.size)
        for(pos in value) {
            stream.writeInt(pos.x)
            stream.writeInt(pos.y)
            stream.writeInt(pos.z)
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound, name: String) {
        val tag = NBTTagList()
        for(pos in value) tag.appendTag(NBTUtil.createPosTag(pos))
        nbt.setTag(name, tag)
    }

    override fun readFromNBT(nbt: NBTTagCompound, name: String) {
        val result = mutableListOf<BlockPos>()
        val listTag = nbt.getTagList(name, 10)
        (0..listTag.tagCount() - 1).map { listTag.getCompoundTagAt(it) }.mapTo(result) { NBTUtil.getPosFromTag(it) }
        value = result
    }
}