package org.generousg.fruitylib.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.items.IItemHandler
import java.util.concurrent.Callable


interface IExtendedItemHandler : IItemHandler {
    companion object {
        @CapabilityInject(IItemHandler::class)
        lateinit var PARENT_CAPABILITY: Capability<IItemHandler>
    }

    fun isItemValidForSlot(slot: Int, item: ItemStack): Boolean
    fun isEmpty(): Boolean
    fun clear()
    //Make sure to implement with getter method
    val stacks: NonNullList<ItemStack>

    class Storage : Capability.IStorage<IExtendedItemHandler> {
        override fun readNBT(capability: Capability<IExtendedItemHandler>?, instance: IExtendedItemHandler, side: EnumFacing?, nbt: NBTBase?) {
            PARENT_CAPABILITY.readNBT(instance, side, nbt)
        }

        override fun writeNBT(capability: Capability<IExtendedItemHandler>?, instance: IExtendedItemHandler?, side: EnumFacing?): NBTBase? {
            return PARENT_CAPABILITY.writeNBT(instance, side)
        }
    }

    class Factory : Callable<IExtendedItemHandler> {
        override fun call(): IExtendedItemHandler {
            return GenericItemHandler(1)
        }
    }
}