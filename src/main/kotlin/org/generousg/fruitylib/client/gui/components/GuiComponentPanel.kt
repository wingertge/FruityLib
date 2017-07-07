package org.generousg.fruitylib.client.gui.components

import com.google.common.base.Objects
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import org.generousg.fruitylib.client.gui.misc.BoxRenderer
import org.generousg.fruitylib.client.gui.misc.ISlotBackgroundRenderer
import org.lwjgl.opengl.GL11


class GuiComponentPanel(x: Int, y: Int, width: Int, height: Int, private val container: Container) : GuiComponentResizableComposite(x, y, width, height) {
    override val width: Int get() = _width
    override val height: Int get() = _height

    companion object {
        private val BOX_RENDERER = BoxRenderer(0, 5)

        val normalSlot = object: ISlotBackgroundRenderer {
            override fun render(gui: Gui, slot: Slot) = gui.drawTexturedModalRect(slot.xPos -  1, slot.yPos - 1, 0, 20, 18, 18)
        }

        val bigSlot = object: ISlotBackgroundRenderer {
            override fun render(gui: Gui, slot: Slot) = gui.drawTexturedModalRect(slot.xPos - 5, slot.yPos - 5, 29, 20, 26, 26)
        }

        val noRenderSlot = object: ISlotBackgroundRenderer {
            override fun render(gui: Gui, slot: Slot) = Unit
        }

        fun coloredSlot(color: Int): ISlotBackgroundRenderer {
            return object: ISlotBackgroundRenderer {
                override fun render(gui: Gui, slot: Slot) {
                    GL11.glColor3ub((color shr 16).toByte(), (color shr 8).toByte(), color.toByte())
                    gui.drawTexturedModalRect(slot.xPos - 1, slot.yPos - 1, 0, 20, 18, 18)
                    GL11.glColor3f(1f, 1f, 1f)
                }
            }
        }
    }

    private val slotRenderers = hashMapOf<Int, ISlotBackgroundRenderer>()
    fun setSlotRenderer(slotId: Int, renderer: ISlotBackgroundRenderer) = slotRenderers.put(slotId, renderer)

    override fun renderComponentBackground(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        GL11.glColor3f(1f, 1f, 1f)
        bindComponentSheet()
        BOX_RENDERER.render(this, this.x + x, this.y + y, width, height, (0xFFFFFFFF + Int.MIN_VALUE).toInt())
    }

    override fun renderComponentForeground(minecraft: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        GL11.glColor3f(1f, 1f, 1f)
        bindComponentSheet()

        container.inventorySlots.forEach { Objects.firstNonNull(slotRenderers[it.slotNumber], normalSlot).render(this, it) }
    }
}