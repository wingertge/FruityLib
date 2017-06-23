package org.generousg.fruitylib.integration

import net.minecraftforge.fml.common.Loader
import org.generousg.fruitylib.reflect.SafeClassLoad


object IntegrationUtil {
    fun classExists(clsName: String): Boolean {
        val cls = SafeClassLoad(clsName)
        return cls.tryLoad()
    }

    fun modLoaded(modName: String): Boolean = Loader.isModLoaded(modName)
}