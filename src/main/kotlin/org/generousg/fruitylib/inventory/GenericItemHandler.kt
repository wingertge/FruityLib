package org.generousg.fruitylib.inventory

import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.items.ItemStackHandler
import org.generousg.fruitylib.isNullOrEmpty


open class GenericItemHandler(size: Int) : ItemStackHandler(size), IExtendedItemHandler {
    override fun clear() = super.stacks.clear()

    override fun isEmpty(): Boolean {
        return !super.stacks.any { !it.isNullOrEmpty() }
    }

    override val stacks: NonNullList<ItemStack> get() = super.stacks
    override fun isItemValidForSlot(slot: Int, item: ItemStack): Boolean = true
}