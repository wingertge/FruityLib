package org.generousg.fruitylib.proxy

import com.google.common.base.Optional
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.INetHandler
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.client.FMLClientHandler
import net.minecraftforge.fml.common.network.IGuiHandler
import org.generousg.fruitylib.client.gui.ClientGuiHandler
import java.io.File

class ClientProxy : FruityLibProxy() {
    override val clientWorld: World? get() = Minecraft.getMinecraft().world
    override fun getServerWorld(dimension: Int): World? = DimensionManager.getWorld(dimension)
    override val player: EntityPlayer get() = FMLClientHandler.instance().client.player
    override fun isClientPlayer(player: Entity): Boolean = player is EntityPlayerSP
    override val minecraftDir: File get() = Minecraft.getMinecraft().mcDataDir
    override val logFileName: String get() = "ForgeModLoader-client-0.log"
    override val language: Optional<String> get() = Optional.fromNullable(Minecraft.getMinecraft().gameSettings.language)
    override fun wrapHandler(modSpecificHandler: IGuiHandler): IGuiHandler = ClientGuiHandler(modSpecificHandler)
    override fun setNowPlayingTitle(nowPlaying: String) = Minecraft.getMinecraft().ingameGUI.setRecordPlayingMessage(nowPlaying)

    override fun getTicks(worldObj: World?): Long {
        if(worldObj != null) return worldObj.totalWorldTime
        val cWorld = clientWorld
        if(cWorld != null) return cWorld.totalWorldTime
        return 0
    }

    override fun getPlayerFromHandler(handler: INetHandler): EntityPlayer? {
        if(handler is NetHandlerPlayServer) return handler.playerEntity
        if(handler is NetHandlerPlayClient) return player
        return null
    }

    override fun preInit(event: net.minecraftforge.fml.common.event.FMLPreInitializationEvent) {
        super.preInit(event)
    }

    override fun init(event: net.minecraftforge.fml.common.event.FMLInitializationEvent) {
        super.init(event)
    }

    override fun postInit(event: net.minecraftforge.fml.common.event.FMLPostInitializationEvent) {
        super.postInit(event)
    }
}