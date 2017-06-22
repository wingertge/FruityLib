package org.generousg.fruitylib.config

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraftforge.common.config.Configuration
import java.io.File
import kotlin.reflect.KClass


open class FeatureHelper(modId: String, val mainClass: KClass<*>) {
    val blockHolders = hashSetOf<Class<out BlockInstances>>()
    val itemHolders = hashSetOf<Class<out ItemInstances>>()
    val configProvider : ConfigProvider = ConfigProvider(modId, mainClass)

    fun registerBlocksHolder(holder: Class<out BlockInstances>) {
        blockHolders.add(holder)
    }

    fun registerItemsHolder(holder: Class<out ItemInstances>) {
        itemHolders.add(holder)
    }

    fun preInit(configFile: File) { preInit(Configuration(configFile)) }
    fun preInit(config: Configuration) {
        val features = FeatureManager()
        for (holder in blockHolders)
            features.collectBlocks(holder)
        for(holder in itemHolders)
            features.collectItems(holder)

        registerCustomFeatures(features)

        populateConfig(config)
        val properties = features.loadFromConfig(config)
        FeatureRegistry.instance.register(features, properties)

        if(config.hasChanged()) config.save()
        ConfigStorage.instance.value.register(config)
        configProvider.features = features
        setupIds(configProvider)
        setupBlockFactory(configProvider.blockFactory)
        setupItemFactory(configProvider.itemFactory)
        for (holder in blockHolders)
            configProvider.registerBlocks(holder)
        for(holder in itemHolders)
            configProvider.registerItems(holder)
        setupProvider(configProvider)
    }

    open fun setupItemFactory(itemFactory: FactoryRegistry<Item>) {}
    open fun setupBlockFactory(blockFactory: FactoryRegistry<Block>) {}
    open fun populateConfig(config: Configuration) {}
    open fun registerCustomFeatures(features: FeatureManager) {}
    open fun setupIds(configProvider: ConfigProvider) {}
    open fun setupProvider(configProvider: ConfigProvider) {}
}