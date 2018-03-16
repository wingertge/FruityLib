package org.generousg.fruitylib.integration

import org.generousg.fruitylib.util.Log


object Integration {
    private val modules = arrayListOf<IIntegrationModule>()
    private var alreadyLoaded = false

    fun addModule(module: IIntegrationModule) {
        if(alreadyLoaded) Log.warn("Trying to add integration module ${module.name} after loading. This simply will not do!")
        modules.add(module)
    }

    fun loadModules() {
        if(alreadyLoaded) {
            Log.warn("Trying to load integration modules twice. Ignoring....")
            return
        }

        modules.filter { it.canLoad }.forEach {
            try {
                it.load()
                Log.debug("Loaded integration module ${it.name}")
            } catch (t: Throwable) {
                Log.warn(t, "Can't load integration module ${it.name}")
            }
        }
    }

    fun init() {
        modules.forEach { if(it.canLoad) it.load() }
    }
}
