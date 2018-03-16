package org.generousg.fruitylib.tileentity

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack


interface ICustomPickItem {
    fun getPickBlock(player: EntityPlayer): ItemStack
}

interface ICustomHarvestDrops {
    fun drops(superDrops: List<ItemStack>, fortune: Int): MutableList<ItemStack>
}
