package org.genguava.fruitylib.liquids

import com.google.common.base.Function
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.FluidTankProperties
import net.minecraftforge.fluids.capability.IFluidTankProperties
import org.genguava.fruitylib.util.CollectionUtils
import org.genguava.fruitylib.util.events.Event
import org.genguava.fruitylib.util.events.ValueChangedEvent


open class ProxyTank : IExtendedFluidHandler {
    protected var canFill = true

    protected var canDrain = true

    override fun isEmpty(): Boolean = members.all { it.isEmpty() }
    override fun isFull(): Boolean = members.all { it.isFull() }
    companion object {
        private val FLUID_CONVERTER = Function<Fluid, FluidStack> { input -> FluidStack(input, 0) }

        private val NO_RESTRICTIONS: (FluidStack) -> Boolean = { true }
        private fun filter(vararg acceptableFluids: FluidStack): (FluidStack) -> Boolean {
            if (acceptableFluids.isEmpty()) return NO_RESTRICTIONS
            return { stack -> acceptableFluids.any { it.isFluidEqual(stack) }}
        }
    }

    private var _contents: FluidStack? = null //cached contents
    protected val contents: FluidStack? get() {
        if(members.isEmpty()) return null
        if(_contents == null) {
            val type = members.map { it.tankProperties.first().contents }.firstOrNull()?.fluid ?: return null
            _contents = FluidStack(type, members.map { it.tankProperties.first().contents?.amount ?: 0 }.sum())
        }
        return _contents
    }

    protected val maxAmount: Int get() = members.map { it.tankProperties.first().capacity }.sum()
    private val members = mutableListOf<IExtendedFluidHandler>()
    private val canAccept: (FluidStack) -> Boolean
    constructor() {
        this.canAccept = NO_RESTRICTIONS
    }
    constructor(vararg acceptableFluids: FluidStack) {
        this.canAccept = Companion.filter(*acceptableFluids)
    }

    constructor(vararg acceptableFluids: Fluid) {
        this.canAccept = Companion.filter(*CollectionUtils.transform(acceptableFluids.asList(), FLUID_CONVERTER))
    }

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        members.sortByDescending { it.tankProperties.first().contents?.amount ?: 0 }
        if(resource == null || resource.fluid == null || contents == null || contents!!.isFluidEqual(resource)) return null
        var remaining = resource.amount
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.drain(resource, doDrain)?.amount ?: 0
        }

        invalidateCache()
        return FluidStack(resource.fluid, resource.amount - remaining)
    }

    override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? {
        members.sortByDescending { it.tankProperties.first().contents?.amount ?: 0 }
        if(contents == null || contents!!.fluid == null) return null
        var remaining = maxDrain
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.drain(maxDrain, doDrain)?.amount ?: 0
        }

        invalidateCache()
        return FluidStack(contents!!.fluid, maxDrain - remaining)
    }

    override fun drainInternal(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        members.sortByDescending { it.tankProperties.first().contents?.amount ?: 0 }
        if(resource == null || resource.fluid == null || contents == null || contents!!.isFluidEqual(resource)) return null
        var remaining = resource.amount
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.drainInternal(resource, doDrain)?.amount ?: 0
        }

        invalidateCache()
        return FluidStack(resource.fluid, resource.amount - remaining)
    }

    override fun drainInternal(maxDrain: Int, doDrain: Boolean): FluidStack? {
        members.sortByDescending { it.tankProperties.first().contents?.amount ?: 0 }
        if(contents == null || contents!!.fluid == null) return null
        var remaining = maxDrain
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.drainInternal(maxDrain, doDrain)?.amount ?: 0
        }

        invalidateCache()
        return FluidStack(contents!!.fluid, maxDrain - remaining)
    }

    override fun fill(resource: FluidStack?, doFill: Boolean): Int {
        if(resource == null || !canAccept(resource)) return 0
        members.sortBy { it.tankProperties.first().contents?.amount ?: 0 }
        var remaining = resource.amount
        for (it in members.asSequence()) {
            if(remaining <= 0) break
            remaining -= it.fill(resource, doFill)
        }

        invalidateCache()
        return resource.amount - remaining
    }

    override fun fillInternal(resource: FluidStack?, doFill: Boolean): Int {
        if(resource == null || !canAccept(resource)) return 0
        members.sortBy { it.tankProperties.first().contents?.amount ?: 0 }
        var remaining = resource.amount
        for (it in members.asSequence()) {
            if(remaining <= 0) break
            remaining -= it.fillInternal(resource, doFill)
        }

        invalidateCache()
        return resource.amount - remaining
    }

    override fun getTankProperties(): Array<IFluidTankProperties> {
        return arrayOf(FluidTankProperties(contents, maxAmount, canFill, canDrain))
    }

    fun setMembers(tanks: Iterable<IExtendedFluidHandler>) {
        require(!tanks.any { it == this }) { "Trying to add proxy tank to itself!" }
        val oldCapacity = maxAmount
        members.clear()
        members.addAll(tanks)

        invalidateCache()
        capacityChangedEvent.fire(ValueChangedEvent(maxAmount, oldCapacity))
    }

    fun invalidateCache() {
        val oldContents = contents?.copy()
        _contents = null
        fluidChangedEvent.fire(ValueChangedEvent(contents, oldContents))
    }

    val fluidChangedEvent = Event<ValueChangedEvent<FluidStack?>>()
    val capacityChangedEvent = Event<ValueChangedEvent<Int>>()
}
