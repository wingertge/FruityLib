package org.genguava.fruitylib.proxy

import com.google.common.base.Optional
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.INetHandler
import net.minecraft.world.World
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.IGuiHandler
import java.io.File


abstract class FruityLibProxy {
    open fun preInit(event: FMLPreInitializationEvent) {}
    open fun init(event: FMLInitializationEvent) {}
    open fun postInit(event: FMLPostInitializationEvent) {}

    abstract val clientWorld: World?
    abstract fun getServerWorld(dimension: Int): World?
    abstract val player: EntityPlayer?
    abstract fun isClientPlayer(player: Entity): Boolean
    abstract fun getTicks(worldObj: World?): Long
    abstract val minecraftDir: File
    abstract val language: Optional<String>
    abstract val logFileName: String
    abstract fun wrapHandler(modSpecificHandler: IGuiHandler): IGuiHandler
    abstract fun setNowPlayingTitle(nowPlaying: String)
    abstract fun getPlayerFromHandler(handler: INetHandler): EntityPlayer?
}