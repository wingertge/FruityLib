package org.generousg.fruitylib.inventory

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler


interface IExtendedItemHandler : IItemHandler {
    fun isItemValidForSlot(slot: Int, item: ItemStack): Boolean
}