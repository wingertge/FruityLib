package org.genguava.fruitylib.client.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import org.genguava.fruitylib.client.gui.BaseComponent
import org.genguava.fruitylib.client.gui.ComponentGui
import org.genguava.fruitylib.client.gui.IHasTooltip






class GuiComponentTooltip<out T>(val parent: T, val container: ComponentGui) : BaseComponent(0, 0) where T : BaseComponent, T : IHasTooltip {
    override fun render(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {

    }

    override fun renderOverlay(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        if(parent.isMouseOver(mouseX, mouseY)) {
            val text = parent.getText()
            val width = getWidth(mc.fontRenderer, text)
            val height = getHeight(mc.fontRenderer, text)
            var x = offsetX + mouseX
            var y = offsetY + mouseY
            if(x > container.width - width - 7)
                x = mouseX - offsetX - width
            if(y > container.height - height - 8)
                y = mouseY - offsetY - height

            val innerAlpha = -267386864  //very very dark purple
            drawGradientRect(x, y - 1, x + width + 6, y, innerAlpha, innerAlpha)
            drawGradientRect(x, y + height + 6, x + width + 6, y + height + 7, innerAlpha, innerAlpha)
            drawGradientRect(x, y, x + width + 6, y + height + 6, innerAlpha, innerAlpha)
            drawGradientRect(x - 1, y, x, y + height + 6, innerAlpha, innerAlpha)
            drawGradientRect(x + width + 6, y, x + width + 7, y + height + 6, innerAlpha, innerAlpha)

            //render the background outside box
            val outerAlpha1 = 0x505000FF
            val outerAlpha2 = outerAlpha1 and 0xFEFEFE shr 1 or (outerAlpha1 and -0x1000000)
            drawGradientRect(x, y + 1, x + 1, y + height + 6 - 1, outerAlpha1, outerAlpha2)
            drawGradientRect(x + width + 5, y + 1, x + width + 7, y + height + 6 - 1, outerAlpha1, outerAlpha2)
            drawGradientRect(x, y, x + width + 3, y + 1, outerAlpha1, outerAlpha1)
            drawGradientRect(x, y + height + 5, x + width + 7, y + height + 6, outerAlpha2, outerAlpha2)

            text.withIndex().forEach { (i, s) ->
                mc.fontRenderer.drawStringWithShadow(s, (x + 2).toFloat(), (y + 2 + i * (mc.fontRenderer.FONT_HEIGHT + 1)).toFloat(), 0xFFFFFF)
            }
        }
    }

    private fun getWidth(fontRenderer: FontRenderer, text: List<String>): Int = text.map { fontRenderer.getStringWidth(it) }.max()!!
    private fun getHeight(fontRenderer: FontRenderer, text: List<String>): Int {
        var tooltipHeight = fontRenderer.FONT_HEIGHT - 1
        if (text.size > 1) tooltipHeight += 2 + (text.size - 1) * (fontRenderer.FONT_HEIGHT + 1)
        return tooltipHeight
    }
}
