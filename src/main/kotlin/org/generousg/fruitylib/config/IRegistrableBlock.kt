package org.generousg.fruitylib.config

import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import kotlin.reflect.KClass


interface IRegistrableBlock {
    fun setupBlock(modId: String, blockName: String, tileEntity: KClass<out TileEntity>, itemClass: KClass<out ItemBlock>)
}