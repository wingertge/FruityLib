package org.genguava.fruitylib.client.gui.slots

import net.minecraft.inventory.IInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack


class RestrictedSlot(inventory: IInventory, slot: Int, x: Int, y: Int) : Slot(inventory, slot, x, y) {
    private val inventoryIndex = slot
    override fun isItemValid(stack: ItemStack): Boolean = inventory.isItemValidForSlot(inventoryIndex, stack)
}