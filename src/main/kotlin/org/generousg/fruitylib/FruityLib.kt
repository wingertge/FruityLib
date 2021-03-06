package org.generousg.fruitylib

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLInterModComms
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.generousg.fruitylib.config.ConfigStorage
import org.generousg.fruitylib.flowcontrol.EventQueue
import org.generousg.fruitylib.integration.Integration
import org.generousg.fruitylib.integration.IntegrationUtil
import org.generousg.fruitylib.inventory.IExtendedItemHandler
import org.generousg.fruitylib.liquids.IExtendedFluidHandler
import org.generousg.fruitylib.network.IdSyncManager
import org.generousg.fruitylib.network.event.NetworkEventManager
import org.generousg.fruitylib.network.rpc.RpcCallDispatcher
import org.generousg.fruitylib.network.rpc.targets.EntityRpcTarget
import org.generousg.fruitylib.network.rpc.targets.SyncRpcTarget
import org.generousg.fruitylib.network.rpc.targets.TileEntityRpcTarget
import org.generousg.fruitylib.proxy.FruityLibProxy
import org.generousg.fruitylib.sync.SyncChannelHolder
import org.generousg.fruitylib.util.ItemUtils
import org.generousg.fruitylib.util.Log


@Mod(modid = "fruitylib")
class FruityLib {
    companion object {
        @SidedProxy(clientSide = "org.generousg.fruitylib.proxy.ClientProxy", serverSide = "org.generousg.fruitylib.proxy.ServerProxy")
        lateinit var proxy: FruityLibProxy
        val eventQueue = EventQueue()
        val FRUITY_LIB_TE_GUI = ""
        var DEBUG_MODE = false
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        registerCapabilities()
        SyncChannelHolder.ensureLoaded()
        NetworkEventManager.instance.startRegistration()
        RpcCallDispatcher.instance.startRegistration()
                .registerTargetWrapper(EntityRpcTarget::class.java)
                .registerTargetWrapper(TileEntityRpcTarget::class.java)
                .registerTargetWrapper(SyncRpcTarget.SyncEntityRpcTarget::class.java)
                .registerTargetWrapper(SyncRpcTarget.SyncTileEntityRpcTarget::class.java)
        Log.debug("test")

        /*val configFile = event.suggestedConfigurationFile
        val config = Configuration(configFile)
        ConfigProcessing.processAnnotations("fruitylib", config, LibConfig::class.java)*/

        MinecraftForge.EVENT_BUS.register(ConfigStorage.instance)
        proxy.preInit(event)
        eventQueue.preInit(event)

        Integration.addModule(IntegrationUtil.createSimpleModule("fl_waila", {
            FMLInterModComms.sendMessage("waila", "register", "org.generousg.fruitylib.integration.WailaRegistration.registerCallbacks")
        }))
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        Integration.init()
        proxy.init(event)
        eventQueue.init(event)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy.postInit(event)
        eventQueue.postInit(event)

        NetworkEventManager.instance.finalizeRegistration()
        RpcCallDispatcher.instance.finishRegistration()

        //after all builders are done
        IdSyncManager.instance.value.finishLoading()
        ItemUtils.fixVanillaFuelValues()
    }

    private fun registerCapabilities() {
        CapabilityManager.INSTANCE.register(IExtendedItemHandler::class.java, IExtendedItemHandler.Storage(), IExtendedItemHandler.Factory())
        CapabilityManager.INSTANCE.register(IExtendedFluidHandler::class.java, IExtendedFluidHandler.Storage(), IExtendedFluidHandler.Factory())
    }
}
