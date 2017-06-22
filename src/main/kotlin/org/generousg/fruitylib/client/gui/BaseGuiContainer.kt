package org.generousg.fruitylib.client.gui

import net.minecraft.client.resources.I18n
import org.generousg.fruitylib.client.gui.components.GuiComponentPanel
import org.generousg.fruitylib.container.ContainerBase


@Suppress("UNCHECKED_CAST")
open class BaseGuiContainer<out T : ContainerBase<*>>(container: T, width: Int, height: Int, val name: String) : ComponentGui(container, width, height) {
    override fun createRoot(): BaseComposite = GuiComponentPanel(0, 0, xSize, ySize, getContainer())

    fun getContainer(): T = inventorySlots as T

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        val machineName = I18n.format(name)
        val x = xSize / 2 - (fontRendererObj.getStringWidth(machineName) / 2)
        fontRendererObj.drawString(machineName, x, 6, 4210752)
        val translatedName = I18n.format("container.inventory")
        fontRendererObj.drawString(translatedName, 8, this.ySize - 96 + 2, 4210752)
    }

    fun sendButtonClick(buttonId: Int) = mc.playerController.sendEnchantPacket(getContainer().windowId, buttonId)
}