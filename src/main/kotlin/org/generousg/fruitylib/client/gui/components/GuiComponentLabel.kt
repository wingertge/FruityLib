package org.generousg.fruitylib.client.gui.components

import com.google.common.base.Strings
import com.google.common.collect.ImmutableList
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import org.generousg.fruitylib.client.gui.BaseComponent
import org.lwjgl.opengl.GL11


open class GuiComponentLabel(x: Int, y: Int, private var maxWidth: Int, private var maxHeight: Int, private var text: String?) : BaseComponent(x, y) {
    private var scale = 1f
    private var formattedText: List<String>? = null
    private var additionalLineHeight = 0
    private var tooltip: List<String>? = null

    private val fontRenderer: FontRenderer
        get() = Minecraft.getMinecraft().fontRenderer

    constructor(x: Int, y: Int, text: String) : this(x, y, Minecraft.getMinecraft().fontRenderer.getStringWidth(text), Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT, text)

    protected fun getFormattedText(fr: FontRenderer): List<String> {
        if (formattedText == null) {
            formattedText = if (Strings.isNullOrEmpty(text))
                ImmutableList.of<String>()
            else
                ImmutableList.copyOf(fr.listFormattedStringToWidth(text!!, getMaxWidth()))
        }
        return formattedText!!
    }

    override fun render(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        val fontRenderer = mc.fontRenderer

        if (maxHeight < fontRenderer.FONT_HEIGHT) return
        if (getMaxWidth() < fontRenderer.getCharWidth('m')) return
        GL11.glPushMatrix()
        GL11.glTranslated((offsetX + x).toDouble(), (offsetY + y).toDouble(), 1.0)
        GL11.glScalef(scale, scale, 1f)
        var offset = 0
        for ((lineCount, s) in getFormattedText(fontRenderer).withIndex()) {
            fontRenderer.drawString(s, 0, offset, 4210752)
            offset += fontHeight
            if (lineCount + 1 >= maxLines) break
        }
        GL11.glPopMatrix()
    }

    override fun renderOverlay(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        if (tooltip != null && !tooltip!!.isEmpty() && isMouseOver(mouseX, mouseY)) {
            var lineOffset = 0
            for ((count, it) in tooltip!!.withIndex()) {
                drawHoveringText(it, offsetX + mouseX, offsetY + mouseY, mc.fontRenderer)
                lineOffset += fontHeight
                if(count + 1 >= maxLines) break
            }
        }
    }

    private fun calculateHeight(): Int {
        val fr = fontRenderer
        var offset = 0
        for ((lineCount, _) in getFormattedText(fr).withIndex()) {
            offset += fontHeight
            if (lineCount + 1 >= maxLines) break
        }
        return offset
    }

    private fun calculateWidth(): Int {
        val fr = fontRenderer
        val maxWidth = getFormattedText(fr)
                .map { fr.getStringWidth(it) }
                .max()
                ?: 0
        return (maxWidth * scale).toInt()
    }

    fun setScale(scale: Float): GuiComponentLabel {
        this.formattedText = null
        this.scale = scale
        return this
    }

    fun getScale(): Float {
        return scale
    }

    fun setMaxHeight(maxHeight: Int): GuiComponentLabel {
        this.maxHeight = maxHeight
        return this
    }

    fun setAdditionalLineHeight(lh: Int) {
        this.additionalLineHeight = lh
    }

    val fontHeight: Int
        get() = fontRenderer.FONT_HEIGHT + additionalLineHeight

    fun setMaxWidth(maxWidth: Int): GuiComponentLabel {
        this.maxWidth = maxWidth
        return this
    }

    val maxLines: Int
        get() = Math.floor((maxHeight.toFloat() / scale / fontHeight.toFloat()).toDouble()).toInt()

    fun getMaxWidth(): Int {
        return (this.maxWidth / scale).toInt()
    }

    override val height: Int
        get() = (Math.min(maxHeight, calculateHeight()) + 0.5).toInt()

    override val width: Int
        get() = (Math.min(getMaxWidth(), calculateWidth()) + 0.5).toInt()

    fun getText(): String {
        return text ?: "null"
    }

    fun setText(text: String) {
        this.formattedText = null
        this.text = Strings.nullToEmpty(text)
    }

    val isOverflowing: Boolean
        get() {
            val fr = fontRenderer
            return getFormattedText(fr).size > maxLines
        }

    fun setTooltip(tooltip: List<String>) {
        this.tooltip = tooltip
    }

    fun clearTooltip() {
        this.tooltip = null
    }

    fun setValue(value: String) {
        setText(value)
    }
}