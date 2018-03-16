package org.generousg.fruitylib.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.generousg.fruitylib.util.events.Event
import org.generousg.fruitylib.util.events.KeyPressedEvent
import org.generousg.fruitylib.util.events.MouseClickEvent
import org.generousg.fruitylib.util.events.MouseDragEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12


abstract class BaseComponent(var x: Int, var y: Int) : Gui() {
    companion object {
        protected val ITEM_RENDERER = lazy { Minecraft.getMinecraft().renderItem }
        val TEXTURE_SHEET = ResourceLocation("fruitylib", "textures/gui/components.png")

        private val CRAZY_1: Int = 0x505000FF + Int.MIN_VALUE
        private val CRAZY_2: Int = (CRAZY_1 and (0xFEFEFE + Int.MIN_VALUE)) shr 1 or CRAZY_1 and (-0xFF000000 + Int.MIN_VALUE).toInt()
        private val CRAZY_3: Int = (0xF0100010 + Int.MIN_VALUE).toInt()

        fun bindComponentSheet() = TextureUtils.bindTextureToClient(TEXTURE_SHEET)


    }

    open var enabled = true
    open val width: Int = 0
    open val height: Int = 0

    val keyEvent = Event<KeyPressedEvent>()
    val mouseDownEvent = Event<MouseClickEvent>()
    val mouseUpEvent = Event<MouseClickEvent>()
    val mouseDragEvent = Event<MouseDragEvent>()

    abstract fun render(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int)
    abstract fun renderOverlay(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int)

    open fun isTicking(): Boolean = false
    open fun tick() {}

    fun isMouseOver(mouseX: Int, mouseY: Int) = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height

    private fun drawFancyBox(width: Int, left: Int, top: Int, height: Int) {
        drawGradientRect(left - 3, top - 4, left + width + 3, top - 3, CRAZY_3, CRAZY_3)
        drawGradientRect(left - 3, top + height + 3, left + width + 3, top + height + 4, CRAZY_3, CRAZY_3)
        drawGradientRect(left - 3, top - 3, left + width + 3, top + height + 3, CRAZY_3, CRAZY_3)
        drawGradientRect(left - 4, top - 3, left - 3, top + height + 3, CRAZY_3, CRAZY_3)
        drawGradientRect(left + width + 3, top - 3, left + width + 4, top + height + 3, CRAZY_3, CRAZY_3)

        drawGradientRect(left - 3, top - 3 + 1, left - 3 + 1, top + height + 3 - 1, CRAZY_1, CRAZY_2)
        drawGradientRect(left + width + 2, top - 3 + 1, left + width + 3, top + height + 3 - 1, CRAZY_1, CRAZY_2)
        drawGradientRect(left - 3, top - 3, left + width + 3, top - 3 + 1, CRAZY_1, CRAZY_1)
        drawGradientRect(left - 3, top + height + 2, left + width + 3, top + height + 3, CRAZY_2, CRAZY_2)
    }

    protected fun drawHoveringText(line: String, x: Int, y: Int, font: FontRenderer) {
        GL11.glDisable(GL12.GL_RESCALE_NORMAL)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        this.zLevel = 350.0F
        val width = font.getStringWidth(line)
        drawFancyBox(width, x + 12, y - 12, 8)
        font.drawStringWithShadow(line, (x + 12).toFloat(), (y - 12).toFloat(), -1)
        this.zLevel = 0.0F
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    }

    protected fun drawHoveringText(lines: ArrayList<String>, x: Int, y: Int, font: FontRenderer) {
        val linecount = lines.size
        if(linecount == 0) return

        GL11.glDisable(GL12.GL_RESCALE_NORMAL)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        val width = lines
                .map { font.getStringWidth(it) }
                .max()
                ?: 0

        val left = x + 12
        var top = y - 12

        var height = 8
        if(linecount > 1) height += 2 + (linecount - 1) * 10

        zLevel = 350f

        drawFancyBox(width, left, top, height)

        for(i in 0..linecount-1) {
            val line = lines[i]
            font.drawStringWithShadow(line, left.toFloat(), top.toFloat(), -1)
            if(i == 0) top += 2
            top += 10
        }

        zLevel = 0f
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glEnable(GL12.GL_RESCALE_NORMAL)
    }

    fun drawItemStack(stack: ItemStack, x: Int, y: Int, overlayText: String) {
        zLevel = 200f
        RenderHelper.enableGUIStandardItemLighting()
        GL11.glColor3f(1f, 1f, 1f)
        GL11.glEnable(GL11.GL_NORMALIZE)
        val font = stack.item.getFontRenderer(stack) ?: Minecraft.getMinecraft().fontRenderer
        ITEM_RENDERER.value.renderItemAndEffectIntoGUI(stack, x, y)
        ITEM_RENDERER.value.renderItemOverlayIntoGUI(font, stack, x, y, overlayText)
        zLevel = 0f
        RenderHelper.disableStandardItemLighting()
    }
}