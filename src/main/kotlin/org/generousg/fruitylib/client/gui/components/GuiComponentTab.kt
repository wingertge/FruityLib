package org.generousg.fruitylib.client.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import org.generousg.fruitylib.client.gui.misc.BoxRenderer
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12


open class GuiComponentTab(private val color: Int, private val iconStack: ItemStack, protected val expandedWidth: Int, protected val expandedHeight: Int) : GuiComponentResizableComposite(-5, 0, GuiComponentTab.FOLDED_WIDTH, GuiComponentTab.FOLDED_HEIGHT) {
    private var active = false
    private var dWidth = FOLDED_WIDTH.toDouble()
    private var dHeight = FOLDED_HEIGHT.toDouble()
    
    override val width: Int get() = _width
    override val height: Int get() = _height

    override fun areChildrenActive(): Boolean {
        return active && width == expandedWidth && height == expandedHeight
    }

    override fun renderComponentBackground(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        val renderItem = minecraft.renderItem
        val targetWidth = (if (active) expandedWidth else FOLDED_WIDTH).toDouble()
        val targetHeight = (if (active) expandedHeight else FOLDED_HEIGHT).toDouble()
        if (width.toDouble() != targetWidth) dWidth += (targetWidth - dWidth) / 4
        if (height.toDouble() != targetHeight) dHeight += (targetHeight - dHeight) / 4

        _width = Math.round(dWidth).toInt()
        _height = Math.round(dHeight).toInt()

        bindComponentSheet()
        BOX_RENDERER.render(this, offsetX + x, offsetY + y, width, height, color)

        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderHelper.enableGUIStandardItemLighting()
        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
        renderItem.renderItemAndEffectIntoGUI(iconStack,
                offsetX + x + 3, offsetY + y + 3)
        GL11.glColor3f(1f, 1f, 1f)
        GL11.glDisable(GL12.GL_RESCALE_NORMAL)
        GL11.glDisable(GL11.GL_LIGHTING)
    }

    fun isOrigin(x: Int, y: Int): Boolean {
        return x < FOLDED_WIDTH && y < FOLDED_WIDTH
    }

    fun setActive(active: Boolean) {
        this.active = active
    }

    companion object {

        private val FOLDED_WIDTH = 24
        private val FOLDED_HEIGHT = 24

        private val BOX_RENDERER = object : BoxRenderer(0, 5) {
            override fun renderTopLeftCorner(gui: Gui) {}
            override fun renderBottomLeftCorner(gui: Gui, height: Int) {}
            override fun renderLeftEdge(gui: Gui, height: Int) {}
        }
    }

}