package org.genguava.fruitylib.client.render

import codechicken.lib.texture.TextureUtils
import net.minecraft.client.renderer.block.model.IBakedModel
import net.minecraft.client.renderer.block.model.ItemCameraTransforms
import net.minecraft.client.renderer.block.model.ItemOverrideList
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.ResourceLocation


abstract class FruityBakedModel : IBakedModel {
    abstract val textureLocation: ResourceLocation

    override fun getParticleTexture(): TextureAtlasSprite = TextureUtils.getTexture(textureLocation)
    override fun getItemCameraTransforms(): ItemCameraTransforms = ItemCameraTransforms.DEFAULT
    override fun isBuiltInRenderer(): Boolean = false
    override fun isAmbientOcclusion(): Boolean = true
    override fun isGui3d(): Boolean = false
    override fun getOverrides(): ItemOverrideList = ItemOverrideList(mutableListOf())
}