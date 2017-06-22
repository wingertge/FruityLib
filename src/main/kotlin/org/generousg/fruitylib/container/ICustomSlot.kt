package org.generousg.fruitylib.container

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemStack


interface ICustomSlot {
    fun onClick(player: EntityPlayer, clickType: ClickType, dragType: Int): ItemStack
    fun canDrag(): Boolean
    fun canTransferItemsOut(): Boolean
    fun canTransferItemsIn(): Boolean
}