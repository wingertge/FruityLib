package org.generousg.fruitylib.items

import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World


open class FruityItem : Item() {
    var hasInfo = false

    override fun addInformation(stack: ItemStack, playerIn: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        if(hasInfo) tooltip.add(I18n.format("$unlocalizedName.info"))
    }
}