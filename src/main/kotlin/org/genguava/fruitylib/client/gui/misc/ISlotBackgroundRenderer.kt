package org.genguava.fruitylib.client.gui.misc

import net.minecraft.client.gui.Gui
import net.minecraft.inventory.Slot


interface ISlotBackgroundRenderer {
    fun render(gui: Gui, slot: Slot)
}