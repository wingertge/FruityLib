package org.generousg.fruitylib.util

import net.minecraft.item.ItemStack


class InventoryUtils {
    companion object {
        fun tryMergeStacks(stackToMerge: ItemStack, stackInSlot: ItemStack): Boolean {
            if(stackInSlot.isEmpty || !stackInSlot.isItemEqual(stackToMerge) || !ItemStack.areItemStackTagsEqual(stackToMerge, stackInSlot)) return false

            var newStackSize = stackInSlot.count + stackToMerge.count
            val maxStackSize = stackToMerge.maxStackSize

            if(newStackSize <= maxStackSize) {
                stackToMerge.count = 0
                stackInSlot.count = newStackSize

                return true
            } else if(stackInSlot.count < maxStackSize) {
                stackToMerge.count -= maxStackSize - stackInSlot.count
                stackInSlot.count = maxStackSize
                return true
            }
            return false
        }
     }
}