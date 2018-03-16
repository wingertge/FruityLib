package org.genguava.fruitylib.config

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Maps
import net.minecraftforge.common.config.Configuration
import java.io.File
import java.lang.reflect.Field
import java.util.*


class ConfigProcessing {
    class ModConfig (val modId: String, val config: Configuration, val configClass: Class<*>){
        private val properties = HashBasedTable.create<String, String, ConfigPropertyMeta>()

        fun tryProcessConfig(field: Field) {
            val meta = ConfigPropertyMeta.createMetaForField(config, field)
            if(meta != null) {
                meta.updateValueFromConfig(false)
                properties.put(meta.category.toLowerCase(Locale.ENGLISH), meta.name.toLowerCase(Locale.ENGLISH), meta)
            }
        }

        fun getConfigFile(): File = config.configFile
        fun save() {
            if(config.hasChanged()) config.save()
        }
        fun getCategories(): Collection<String> = Collections.unmodifiableCollection(properties.rowKeySet())
        fun getValues(category: String): Collection<String> = Collections.unmodifiableCollection(properties.row(category.toLowerCase(Locale.ENGLISH)).keys)
        fun getValue(category: String, name: String): ConfigPropertyMeta = properties.get(category.toLowerCase(Locale.ENGLISH), name.toLowerCase(Locale.ENGLISH))
    }

    companion object {
        private val configs = Maps.newHashMap<String, ModConfig>()

        fun getConfigIds(): Collection<String> = Collections.unmodifiableCollection(configs.keys)
        fun getConfig(modId: String): ModConfig? = configs[modId.toLowerCase(Locale.ENGLISH)]

        fun processAnnotations(modId: String, config: Configuration, clazz: Class<*>) {
            require(!configs.containsKey(modId)) { "Trying to configure mod '$modId' twice" }
            val modConfig = ModConfig(modId, config, clazz)
            configs.put(modId.toLowerCase(Locale.ENGLISH), modConfig)
            for(field in clazz.fields)
                modConfig.tryProcessConfig(field)
        }
    }
}