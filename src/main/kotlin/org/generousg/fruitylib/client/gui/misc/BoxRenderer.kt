package org.generousg.fruitylib.client.gui.misc

import net.minecraft.client.gui.Gui
import org.lwjgl.opengl.GL11


open class BoxRenderer(val u: Int, val v: Int) {
    protected open fun renderTopLeftCorner(gui: Gui) = gui.drawTexturedModalRect(0, 0, u, v, 4, 4)
    protected fun renderTopRightCorner(gui: Gui, width: Int) = gui.drawTexturedModalRect(width - 3, 0, u + 5, v, 3, 3)
    protected open fun renderBottomLeftCorner(gui: Gui, height: Int) = gui.drawTexturedModalRect(1, height - 3, u + 12, v, 2, 2)
    protected fun renderBottomRightCorner(gui: Gui, width: Int, height: Int) = gui.drawTexturedModalRect(width - 4, height - 4, u + 15, v, 4, 4)
    protected fun renderBottomEdge(gui: Gui, width: Int, height: Int) {
        GL11.glPushMatrix()
        GL11.glTranslatef(3f, (height - 3).toFloat(), 0f)
        GL11.glScaled((width - 7).toDouble(), 1.0, 0.0)
        gui.drawTexturedModalRect(0, 0, u + 15, v + 1, 1, 3)
        GL11.glPopMatrix()
    }

    protected fun renderTopEdge(gui: Gui, width: Int) {
        GL11.glPushMatrix()
        GL11.glTranslatef(4f, 0f, 0f)
        GL11.glScaled((width - 7).toDouble(), 1.0, 0.0)
        gui.drawTexturedModalRect(0, 0, u + 4, v, 1, 3)
        GL11.glPopMatrix()
    }

    protected open fun renderLeftEdge(gui: Gui, height: Int) {
        GL11.glPushMatrix()
        GL11.glTranslatef(0f, 4f, 0f)
        GL11.glScaled(1.0, (height - 7).toDouble(), 0.0)
        gui.drawTexturedModalRect(0, 0, u, v + 4, 3, 1)
        GL11.glPopMatrix()
    }

    protected fun renderRightEdge(gui: Gui, width: Int, height: Int) {
        GL11.glPushMatrix()
        GL11.glTranslatef((width - 3).toFloat(), 3f, 0f)
        GL11.glScaled(1.0, (height - 7).toDouble(), 0.0)
        gui.drawTexturedModalRect(0, 0, u + 8, v, 3, 1)
        GL11.glPopMatrix()
    }

    protected fun renderBackground(gui: Gui, width: Int, height: Int) {
        GL11.glPushMatrix()
        GL11.glTranslatef(3f, 3f, 0f)
        GL11.glScalef((width - 6).toFloat(), (height - 6).toFloat(), 0f)
        gui.drawTexturedModalRect(0, 0, u + 19, v, 1, 1)
        GL11.glPopMatrix()
    }

    fun render(gui: Gui, x: Int, y: Int, width: Int, height: Int, color: Int) {
        setColor(color)

        GL11.glPushMatrix()
        GL11.glTranslatef(x.toFloat(), y.toFloat(), 0f)
        renderBackground(gui, width, height)
        renderTopEdge(gui, width)
        renderBottomEdge(gui, width, height)
        renderLeftEdge(gui, height)
        renderRightEdge(gui, width, height)

        renderTopLeftCorner(gui)
        renderTopRightCorner(gui, width)
        renderBottomLeftCorner(gui, height)
        renderBottomRightCorner(gui, width, height)
        GL11.glPopMatrix()
    }

    private fun setColor(color: Int) {
        val r = (color shr 16).toByte()
        val g = (color shr 8).toByte()
        val b = color.toByte()
        GL11.glColor3ub(r, g, b)
    }
}