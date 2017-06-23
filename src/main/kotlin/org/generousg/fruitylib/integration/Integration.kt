package org.generousg.fruitylib.integration

import net.minecraftforge.fml.common.event.FMLInterModComms
import org.generousg.fruitylib.util.Log


object Integration {
    private val modules = arrayListOf<IIntegrationModule>()
    private var alreadyLoaded = false

    fun addModule(module: IIntegrationModule) {
        if(alreadyLoaded) Log.warn("Trying to add org.generousg.fruitylib.integration module ${module.name} after loading. This simply will not do!")
        modules.add(module)
    }

    fun loadModules() {
        if(alreadyLoaded) {
            Log.warn("Trying to load org.generousg.fruitylib.integration modules twice. Ignoring....")
            return
        }

        modules.filter { it.canLoad }.forEach {
            try {
                it.load()
                Log.debug("Loaded org.generousg.fruitylib.integration module ${it.name}")
            } catch (t: Throwable) {
                Log.warn(t, "Can't load org.generousg.fruitylib.integration module ${it.name}")
            }
        }
    }

    fun init() {
        FMLInterModComms.sendMessage("waila", "register", "org.generousg.fruitylib.integration.WailaRegistration.registerCallbacks")

    }
}