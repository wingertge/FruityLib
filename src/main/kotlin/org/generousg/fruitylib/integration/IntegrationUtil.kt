package org.generousg.fruitylib.integration

import net.minecraftforge.fml.common.Loader
import org.generousg.fruitylib.reflect.SafeClassLoad


object IntegrationUtil {
    fun classExists(clsName: String): Boolean {
        val cls = SafeClassLoad(clsName)
        return cls.tryLoad()
    }

    fun modLoaded(modName: String): Boolean = Loader.isModLoaded(modName)

    fun createSimpleModule(name: String, load: ()->Unit): IIntegrationModule {
        return object: IIntegrationModule {
            override val canLoad: Boolean = true
            override val name: String = name

            override fun load() = load()
        }
    }
}
