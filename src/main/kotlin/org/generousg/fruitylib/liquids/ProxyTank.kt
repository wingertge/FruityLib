package org.generousg.fruitylib.liquids

import com.google.common.base.Function
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidTankProperties
import org.generousg.fruitylib.util.CollectionUtils
import org.generousg.fruitylib.util.events.Event
import org.generousg.fruitylib.util.events.ValueChangedEvent


class ProxyTank : IExtendedFluidHandler {
    override fun isEmpty(): Boolean = members.all { it.isEmpty() }
    override fun isFull(): Boolean = members.all { isFull() }

    companion object {
        private val FLUID_CONVERTER = Function<Fluid, FluidStack> { input -> FluidStack(input, 0) }
        private val NO_RESTRICTIONS: (FluidStack) -> Boolean = { true }

        private fun filter(vararg acceptableFluids: FluidStack): (FluidStack) -> Boolean {
            if (acceptableFluids.isEmpty()) return NO_RESTRICTIONS
            return { stack -> acceptableFluids.any { it.isFluidEqual(stack) }}
        }
    }

    private var _contents: FluidStack? = null //cached contents
    override val contents: FluidStack? get() {
        if(members.isEmpty()) return null
        if(_contents == null) {
            val type = members.map { it.contents }.firstOrNull()?.fluid ?: return null
            _contents = FluidStack(type, members.map { it.contents?.amount ?: 0 }.sum())
        }
        return _contents
    }
    override val maxAmount: Int
        get() = members.map { it.maxAmount }.sum()
    private val members = mutableListOf<IExtendedFluidHandler>()
    private val filter: (FluidStack) -> Boolean

    constructor() {
        this.filter = NO_RESTRICTIONS
    }

    constructor(vararg acceptableFluids: FluidStack) {
        this.filter = Companion.filter(*acceptableFluids)
    }

    constructor(vararg acceptableFluids: Fluid) {
        this.filter = Companion.filter(*CollectionUtils.transform(acceptableFluids.asList(), FLUID_CONVERTER))
    }

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        val prevAmount = contents?.copy()
        members.sortByDescending { it.contents?.amount ?: 0 }
        if(resource == null || resource.fluid == null || contents == null || contents!!.isFluidEqual(resource)) return null
        var remaining = resource.amount
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.drain(resource, doDrain)?.amount ?: 0
        }

        if(doDrain) _contents = null //contents changed, clear cache
        if(doDrain && prevAmount?.amount != contents?.amount) fluidChangedEvent.fire(ValueChangedEvent(contents, prevAmount))
        return FluidStack(resource.fluid, resource.amount - remaining)
    }

    override fun drain(maxDrain: Int, doDrain: Boolean): FluidStack? {
        val prevAmount = contents?.copy()
        members.sortByDescending { it.contents?.amount ?: 0 }
        if(contents == null || contents!!.fluid == null) return null
        var remaining = maxDrain
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.drain(maxDrain, doDrain)?.amount ?: 0
        }

        if(doDrain) _contents = null //contents changed, clear cache
        if(doDrain && prevAmount?.amount != contents?.amount) fluidChangedEvent.fire(ValueChangedEvent(contents, prevAmount))
        return FluidStack(contents!!.fluid, maxDrain - remaining)
    }

    override fun fill(resource: FluidStack?, doFill: Boolean): Int {
        val prevAmount = contents?.copy()
        if(resource == null || !filter.invoke(resource)) return 0
        members.sortBy { it.contents?.amount ?: 0 }
        var remaining = resource.amount
        members.asSequence().forEach {
            if(remaining <= 0) return@forEach
            remaining -= it.fill(resource, doFill)
        }

        if(doFill) _contents = null //contents changed, clear cache
        if(doFill && prevAmount?.amount != contents?.amount) fluidChangedEvent.fire(ValueChangedEvent(contents, prevAmount))
        return resource.amount - remaining
    }

    override fun getTankProperties(): Array<IFluidTankProperties> {
        return if(!members.isEmpty()) members[0].tankProperties else arrayOf()
    }

    fun setMembers(tanks: Iterable<IExtendedFluidHandler>) {
        require(!tanks.any { it == this }) { "Trying to add proxy tank to itself!" }
        val oldCapacity = maxAmount
        members.clear()
        members.addAll(tanks)

        _contents = null //tanks changed, clear cache
        capacityChangedEvent.fire(ValueChangedEvent(maxAmount, oldCapacity))
    }

    val fluidChangedEvent = Event<ValueChangedEvent<FluidStack?>>()
    val capacityChangedEvent = Event<ValueChangedEvent<Int>>()
}