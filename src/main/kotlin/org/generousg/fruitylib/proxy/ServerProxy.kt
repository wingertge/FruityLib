package org.generousg.fruitylib.proxy

import com.google.common.base.Optional
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.INetHandler
import net.minecraft.network.NetHandlerPlayServer
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.network.IGuiHandler
import org.generousg.fruitylib.client.gui.CommonGuiHandler
import java.io.File

class ServerProxy : FruityLibProxy() {
    override val clientWorld: World? get() = null
    override val player: EntityPlayer? get() = null
    override val minecraftDir: File get() = FMLCommonHandler.instance().minecraftServerInstance.getFile("")
    override val language: Optional<String> get() = Optional.absent()
    override val logFileName: String get() = "ForgeModLoader-server-0.log"

    override fun getServerWorld(dimension: Int): World? = DimensionManager.getWorld(dimension)
    override fun isClientPlayer(player: Entity): Boolean = false
    override fun getTicks(worldObj: World?): Long = worldObj?.totalWorldTime ?: 0
    override fun wrapHandler(modSpecificHandler: IGuiHandler): IGuiHandler = CommonGuiHandler(modSpecificHandler)
    override fun setNowPlayingTitle(nowPlaying: String) = Unit

    override fun getPlayerFromHandler(handler: INetHandler): EntityPlayer? {
        if(handler is NetHandlerPlayServer) return handler.playerEntity
        return null
    }
}