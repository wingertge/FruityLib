package org.generousg.fruitylib.liquids

import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import java.util.concurrent.Callable


interface IExtendedFluidHandler : IFluidHandler {
    companion object {
        @CapabilityInject(IFluidHandler::class)
        lateinit var PARENT_CAPABILITY: Capability<IFluidHandler>
    }

    val contents: FluidStack?
    val maxAmount: Int

    fun isEmpty(): Boolean
    fun isFull(): Boolean

    class Storage : Capability.IStorage<IExtendedFluidHandler> {
        override fun readNBT(capability: Capability<IExtendedFluidHandler>?, instance: IExtendedFluidHandler, side: EnumFacing?, nbt: NBTBase?) {
            PARENT_CAPABILITY.readNBT(instance, side, nbt)
        }

        override fun writeNBT(capability: Capability<IExtendedFluidHandler>?, instance: IExtendedFluidHandler?, side: EnumFacing?): NBTBase? {
            return PARENT_CAPABILITY.writeNBT(instance, side)
        }
    }

    class Factory : Callable<IExtendedFluidHandler> {
        override fun call(): IExtendedFluidHandler {
            return GenericTank(1000)
        }
    }
}