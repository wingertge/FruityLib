package org.generousg.fruitylib.client.gui

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation


class TextureUtils {
    companion object {
        val TEXTURE_MAP_BLOCKS = 0
        val TEXTURE_MAP_ITEMS = 1

        fun bindTextureToClient(texture: ResourceLocation) {
            val mc = Minecraft.getMinecraft()
            mc?.renderEngine?.bindTexture(texture)
        }
    }
}