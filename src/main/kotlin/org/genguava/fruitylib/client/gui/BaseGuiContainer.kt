package org.genguava.fruitylib.client.gui

import net.minecraft.client.resources.I18n
import org.genguava.fruitylib.client.gui.components.GuiComponentPanel
import org.genguava.fruitylib.container.ContainerBase


@Suppress("UNCHECKED_CAST")
open class BaseGuiContainer<out T : ContainerBase<*>>(container: T, width: Int, height: Int, val name: String) : ComponentGui(container, width, height) {
    val xPos get() = (width - xSize) / 2
    val yPos get() = (height - ySize) / 2

    override fun createRoot(): BaseComposite = GuiComponentPanel(0, 0, xSize, ySize, container)

    val container: T get() = inventorySlots as T

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY)
        val machineName = I18n.format(name)
        val x = xSize / 2 - (fontRenderer.getStringWidth(machineName) / 2)
        fontRenderer.drawString(machineName, x, 6, 4210752)
        val translatedName = I18n.format("container.inventory")
        fontRenderer.drawString(translatedName, 8, this.ySize - 96 + 2, 4210752)
    }

    fun sendButtonClick(buttonId: Int) = mc.playerController.sendEnchantPacket(container.windowId, buttonId)
}