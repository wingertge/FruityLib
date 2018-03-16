package org.genguava.fruitylib.client.gui.components

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.genguava.fruitylib.client.gui.IHasTooltip
import org.genguava.fruitylib.client.gui.TextureUtils
import org.genguava.fruitylib.client.gui.misc.BoxRenderer
import org.genguava.fruitylib.util.events.ValueChangedEvent
import org.lwjgl.opengl.GL11


class GuiComponentTankLevel(x: Int, y: Int, width: Int, height: Int, var capacity: Int, val fluidName: String = "") : GuiComponentResizable(x, y, width, height), IHasTooltip {
    override fun getText(): List<String> {
        var fluid = fluidStack?.fluid
        if(fluid == null && fluidName != "") fluid = FluidRegistry.getFluid(fluidName)
        if(fluid == null) return arrayListOf("Empty")
        else return arrayListOf(fluid.getLocalizedName(fluidStack), "${TextFormatting.GRAY}${fluidStack?.amount ?: 0}mB / ${capacity}mB")
    }

    private val BOX_RENDERER = BoxRenderer(0, 0)
    private val BORDER_COLOR = 0xc6c6c6

    var fluidStack: FluidStack? = null

    override fun render(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {
        bindComponentSheet()
        BOX_RENDERER.render(this, x + offsetX, y + offsetY, width, height, BORDER_COLOR)

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE)
        renderTank(fluidStack, capacity, fluidStack?.amount ?: 0, x, y, width, height)
    }

    fun renderTank(fluid: FluidStack?, capacity: Int, amount: Int, x: Int, y: Int, width: Int, height: Int) {
        if (fluid == null || fluid.fluid == null || fluid.amount <= 0) return

        val icon = TextureUtils.getStillTexture(fluid) ?: return

        val renderAmount = Math.max(Math.min(height, amount * height / capacity), 1)
        val posY = (y + height - renderAmount)

        val color = fluid.fluid.getColor(fluid)
        GL11.glColor3ub((color shr 16 and 0xFF).toByte(), (color shr 8 and 0xFF).toByte(), (color and 0xFF).toByte())

        GlStateManager.enableBlend()
        var i = 0
        while (i < width) {
            var j = 0
            while (j < renderAmount) {
                val drawWidth = Math.min(width - i, 16)
                val drawHeight = Math.min(renderAmount - j, 16)

                val drawX = (x + i)
                val drawY = posY + j

                val minU = icon.minU.toDouble()
                val maxU = icon.maxU.toDouble()
                val minV = icon.minV.toDouble()
                val maxV = icon.maxV.toDouble()

                val tessellator = Tessellator.getInstance()
                val tes = tessellator.buffer
                tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
                tes.pos(drawX.toDouble(), drawY.toDouble() + drawHeight, 0.0).tex(minU, minV + (maxV - minV) * drawHeight / 16f).endVertex()
                tes.pos(drawX.toDouble() + drawWidth, drawY.toDouble() + drawHeight, 0.0).tex(minU + (maxU - minU) * drawWidth / 16f, minV + (maxV - minV) * drawHeight / 16f).endVertex()
                tes.pos(drawX.toDouble() + drawWidth, drawY.toDouble(), 0.0).tex(minU + (maxU - minU) * drawWidth / 16f, minV).endVertex()
                tes.pos(drawX.toDouble(), drawY.toDouble(), 0.0).tex(minU, minV).endVertex()
                tessellator.draw()
                j += 16
            }
            i += 16
        }
        GlStateManager.disableBlend()
    }

    override fun renderOverlay(mc: Minecraft, offsetX: Int, offsetY: Int, mouseX: Int, mouseY: Int) {

    }

    val fluidReceiver: (ValueChangedEvent<FluidStack?>)->Unit = { fluidStack = it.value }
    val capacityReceiver: (ValueChangedEvent<Int>)->Unit = { capacity = it.value }
}
