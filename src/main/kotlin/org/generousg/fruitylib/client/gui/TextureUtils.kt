package org.generousg.fruitylib.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack


class TextureUtils {
    companion object {
        val TEXTURE_MAP_BLOCKS = 0
        val TEXTURE_MAP_ITEMS = 1

        fun bindTextureToClient(texture: ResourceLocation) {
            val mc = Minecraft.getMinecraft()
            mc.renderEngine?.bindTexture(texture)
        }

        fun getStillTexture(fluid: FluidStack?): TextureAtlasSprite? {
            if (fluid == null || fluid.fluid == null) {
                return null
            }
            return getStillTexture(fluid.fluid)
        }

        fun getStillTexture(fluid: Fluid): TextureAtlasSprite? {
            val iconKey = fluid.still ?: return null
            return Minecraft.getMinecraft().textureMapBlocks.getTextureExtry(iconKey.toString())
        }

    }
}