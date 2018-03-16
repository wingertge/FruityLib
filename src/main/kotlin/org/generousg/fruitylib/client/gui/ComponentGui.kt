package org.generousg.fruitylib.client.gui

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.inventory.Container
import org.generousg.fruitylib.util.events.KeyPressedEvent
import org.generousg.fruitylib.util.events.MouseClickEvent
import org.generousg.fruitylib.util.events.MouseDragEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12


abstract class ComponentGui(container: Container, width: Int, height: Int) : GuiContainer(container) {
    val root: BaseComposite

    init {
        xSize = width
        ySize = height
        root = createRoot()
    }

    abstract fun createRoot(): BaseComposite

    override fun updateScreen() {
        super.updateScreen()

        if(root.isTicking()) root.tick()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)

        if(root.isMouseOver(mouseX - guiLeft, mouseY - guiTop)) root.mouseDownEvent.fire(MouseClickEvent(root, mouseX, mouseY, mouseButton))
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Int) {
        super.mouseReleased(mouseX, mouseY, button)
        if(root.isMouseOver(mouseX - guiLeft, mouseY - guiTop)) root.mouseUpEvent.fire(MouseClickEvent(root, mouseX, mouseY, button))
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        if(root.isMouseOver(mouseX - guiLeft, mouseY - guiTop)) root.mouseDragEvent.fire(MouseDragEvent(root, mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        root.keyEvent.fire(KeyPressedEvent(root, typedChar, keyCode))
    }

    open fun preRender(mouseX: Float, mouseY: Float) {}
    open fun postRender(mouseX: Float, mouseY: Float) {}

    override fun drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) {
        preRender(mouseX.toFloat(), mouseY.toFloat())
        GL11.glPushMatrix()
        GL11.glTranslated(guiLeft.toDouble(), guiTop.toDouble(), 0.toDouble())
        root.render(mc, 0, 0, mouseX - guiLeft, mouseY - guiTop)
        GL11.glPopMatrix()
    }

    override fun drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        postRender(mouseX.toFloat(), mouseY.toFloat())
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        prepareRenderState()
        GL11.glPushMatrix()
        root.renderOverlay(mc, guiLeft, guiTop, mouseX - guiLeft, mouseY - guiTop)
        GL11.glPopMatrix()
        restoreRenderState()
    }

    protected fun prepareRenderState() {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL)
        RenderHelper.disableStandardItemLighting()
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
    }

    protected fun restoreRenderState() {
        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        RenderHelper.enableStandardItemLighting()
    }
}