package org.generousg.fruitylib.config

import com.google.common.collect.ArrayListMultimap
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.client.event.ConfigChangedEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


class ConfigStorage {
    private object Holder { val INSTANCE = ConfigStorage() }
    companion object { val instance = lazy { Holder.INSTANCE } }

    private val configs = ArrayListMultimap.create<String, Configuration>()

    fun register(value: Configuration) {
        val mod = Loader.instance().activeModContainer()
        requireNotNull(mod) { "Can't register outside initialization" }
        configs.put(mod!!.modId, value)
    }

    fun getConfigs(modId: String): Collection<Configuration> = configs[modId]
    fun saveAll(modId: String) {
        configs[modId]
                .filter { it.hasChanged() }
                .forEach { it.save() }
    }

    @SubscribeEvent
    fun onConfigChange(event: ConfigChangedEvent) = saveAll(event.modID)
}