package org.generousg.fruitylib.items

import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack


open class FruityItem : Item() {
    var hasInfo = false

    override fun addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) {
        if(hasInfo) tooltip.add(I18n.format(unlocalizedName + ".info"))
    }
}