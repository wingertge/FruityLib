package org.genguava.fruitylib.sync

import com.google.common.io.ByteStreams
import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader
import com.sun.xml.internal.stream.writers.UTF8OutputStreamWriter
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.genguava.fruitylib.add
import org.genguava.fruitylib.liquids.GenericTank
import org.genguava.fruitylib.subtract
import org.genguava.fruitylib.util.ByteUtils
import org.genguava.fruitylib.util.events.Event
import org.genguava.fruitylib.util.events.ValueChangedEvent
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import kotlin.reflect.KProperty


open class SyncableTank : GenericTank, ISyncableObject, ISyncableValueProvider<GenericTank> {
    val fluidChangedEvent = Event<ValueChangedEvent<FluidStack?>>()

    override fun getValue(thisRef: Any?, property: KProperty<*>): GenericTank {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: GenericTank) {

    }

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
            val reader = UTF8Reader(stream)
            val fluidIdLength = ByteUtils.readVLI(stream)
            val result = CharArray(fluidIdLength)
            val charsRead = reader.read(result)
            require(charsRead == fluidIdLength) { "Error reading fluid id. Chars read lower than string length." }
            val fluidId = String(result)
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
            val fluidName = FluidRegistry.getFluidName(fluid)
            val writer = UTF8OutputStreamWriter(stream)
            ByteUtils.writeVLI(stream, fluidName.length)
            writer.write(fluidName)
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
        if (doFill && filled > 0) {
            markDirty()
            fluidChangedEvent.fire(ValueChangedEvent(fluid, fluid?.subtract(filled)))
        }
        return filled
    }

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        val drained = super.drain(resource, doDrain)
        if (doDrain && drained != null) {
            markDirty()
            fluidChangedEvent.fire(ValueChangedEvent(fluid, fluid?.add(drained)))
        }
        return drained
    }

    override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? {
        val drained = super.drain(maxDrain, doDrain)
        if (doDrain && drained != null) {
            markDirty()
            fluidChangedEvent.fire(ValueChangedEvent(fluid, fluid?.add(drained)))
        }
        return drained
    }

    override fun fillInternal(resource: FluidStack?, doFill: Boolean): Int {
        val filled = super.fillInternal(resource, doFill)
        if (doFill && filled > 0) {
            markDirty()
            fluidChangedEvent.fire(ValueChangedEvent(fluid, fluid?.subtract(filled)))
        }
        return filled
    }

    override fun drainInternal(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        val drained = super.drainInternal(resource, doDrain)
        if (doDrain && drained != null) {
            markDirty()
            fluidChangedEvent.fire(ValueChangedEvent(fluid, fluid?.add(drained)))
        }
        return drained
    }

    override fun drainInternal(maxDrain: Int, doDrain: Boolean): FluidStack? {
        val drained = super.drainInternal(maxDrain, doDrain)
        if (doDrain && drained != null) {
            markDirty()
            fluidChangedEvent.fire(ValueChangedEvent(fluid, fluid?.add(drained)))
        }
        return drained
    }

    override val value get() = this
}
