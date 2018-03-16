package org.genguava.fruitylib.config

import net.minecraft.item.ItemBlock
import net.minecraft.tileentity.TileEntity
import org.genguava.fruitylib.blocks.ItemFruityBlock
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterBlock(val name: String, val itemBlock: KClass<out ItemBlock> = ItemFruityBlock::class, val tileEntity: KClass<out TileEntity> = TileEntity::class,
                               val unlocalizedName: String = "[default]", val enabled: Boolean = true, val configurable: Boolean = true, val modelName: String = "[default]",
                               val tileEntities: Array<KClass<out TileEntity>> = arrayOf(), val creativeTab: String = "[none]", val hasInfo: Boolean = false)
