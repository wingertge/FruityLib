package org.genguava.fruitylib.api

import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack


interface IPlacerAwareTile {
    fun onBlockPlacedBy(placer: EntityLivingBase, stack: ItemStack)
}
