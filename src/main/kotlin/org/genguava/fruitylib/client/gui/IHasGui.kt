package org.genguava.fruitylib.client.gui

import net.minecraft.entity.player.EntityPlayer


interface IHasGui {
    fun getServerGui(player: EntityPlayer): Any?
    fun getClientGui(player: EntityPlayer): Any?
    fun canOpenGui(player: EntityPlayer): Boolean
}