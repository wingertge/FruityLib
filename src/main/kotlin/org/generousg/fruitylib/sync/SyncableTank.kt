package org.generousg.fruitylib.sync

import com.google.common.io.ByteStreams
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.generousg.fruitylib.liquids.GenericTank
import org.generousg.fruitylib.util.ByteUtils
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException


class SyncableTank : GenericTank, ISyncableObject, ISyncableValueProvider<FluidStack?> {

    private var dirty = false

    constructor(capacity: Int) : super(capacity) {}

    constructor(capacity: Int, vararg acceptableFluids: Fluid) : super(capacity, *acceptableFluids) {}

    constructor(capacity: Int, vararg acceptableFluids: FluidStack) : super(capacity, *acceptableFluids) {}

    override fun isDirty(): Boolean {
        return dirty
    }

    override fun markClean() {
        dirty = false
    }

    override fun markDirty() {
        dirty = true
    }

    @Throws(IOException::class)
    override fun readFromStream(stream: DataInputStream) {
        if (stream.readBoolean()) {
            val fluidIdLength = ByteUtils.readVLI(stream)
            val fluidId = ByteUtils.readString(stream, fluidIdLength)
            val fluid = FluidRegistry.getFluid(fluidId)

            val fluidAmount = stream.readInt()

            this.fluid = FluidStack(fluid, fluidAmount)

            val tagSize = ByteUtils.readVLI(stream)
            if (tagSize > 0) {
                this.fluid!!.tag = CompressedStreamTools.readCompressed(ByteStreams.limit(stream, tagSize.toLong()))
            }

        } else {
            this.fluid = null
        }
    }

    @Throws(IOException::class)
    override fun writeToStream(stream: DataOutputStream) {
        if (fluid != null) {
            stream.writeBoolean(true)
            ByteUtils.writeVLI(stream, fluid!!.unlocalizedName.length)
            stream.write(fluid!!.unlocalizedName.toByteArray())
            stream.writeInt(fluid!!.amount)
            if (fluid!!.tag != null) {
                val buffer = ByteArrayOutputStream()
                CompressedStreamTools.writeCompressed(fluid!!.tag, buffer)

                val bytes = buffer.toByteArray()
                ByteUtils.writeVLI(stream, bytes.size)
                stream.write(bytes)
            } else {
                stream.writeByte(0)
            }
        } else {
            stream.writeBoolean(false)
        }
    }

    override fun writeToNBT(nbt: NBTTagCompound, name: String) {
        val tankTag = NBTTagCompound()
        this.writeToNBT(tankTag)

        nbt.setTag(name, tankTag)
    }

    override fun readFromNBT(nbt: NBTTagCompound, name: String) {
        if (nbt.hasKey(name, Constants.NBT.TAG_COMPOUND)) {
            val tankTag = nbt.getCompoundTag(name)
            this.readFromNBT(tankTag)
        } else {
            // For legacy worlds - tag was saved in wrong place due to bug
            this.readFromNBT(nbt)
        }
    }

    override fun fill(resource: FluidStack?, doFill: Boolean): Int {
        val filled = super.fill(resource, doFill)
        if (doFill && filled > 0) markDirty()
        return filled
    }

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        val drained = super.drain(resource, doDrain)
        if (doDrain && drained != null) markDirty()
        return drained
    }

    override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? {
        val drained = super.drain(maxDrain, doDrain)
        if (doDrain && drained != null) markDirty()
        return drained
    }

    override val value: FluidStack?
        get() {
            val stack = super.getFluid()
            return stack?.copy()
        }
}