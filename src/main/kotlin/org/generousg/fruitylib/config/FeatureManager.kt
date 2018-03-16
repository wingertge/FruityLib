package org.generousg.fruitylib.config

import com.google.common.base.Preconditions
import com.google.common.collect.HashBasedTable
import com.google.common.collect.ImmutableTable
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.common.config.Property


class FeatureManager : AbstractFeatureManager() {
    private class FeatureEntry(var enabled: Boolean, val configurable: Boolean)

    interface CustomFeatureRule {
        fun isEnabled(flag: Boolean): Boolean
    }

    private val customRules = HashBasedTable.create<String, String, CustomFeatureRule>()
    private val features = HashBasedTable.create<String, String, FeatureEntry>()

    fun collectItems(itemContainer: Class<out ItemInstances>) {
        itemContainer.fields
                .mapNotNull { it.getAnnotation(RegisterItem::class.java) }
                .forEach { features.put(CATEGORY_ITEMS, it.name, FeatureEntry(it.enabled, it.configurable)) }
    }

    fun collectBlocks(blockContainer: Class<out BlockInstances>) {
        blockContainer.fields
                .mapNotNull { it.getAnnotation(RegisterBlock::class.java) }
                .forEach { features.put(CATEGORY_BLOCKS, it.name, FeatureEntry(it.enabled, it.configurable))}
    }

    fun collectFluids(fluidContainer: Class<out FluidInstances>) {
        fluidContainer.fields
                .mapNotNull { it.getAnnotation(RegisterFluid::class.java) }
                .forEach { features.put(CATEGORY_FLUIDS, it.name, FeatureEntry(it.enabled, it.configurable)) }
    }

    fun loadFromConfig(config: Configuration): ImmutableTable<String, String, Property> {
        val properties = HashBasedTable.create<String, String, Property>()
        for(cell in features.cellSet()) {
            val entry = cell.value!!
            if(!entry.configurable) continue
            val categoryName = cell.rowKey!!
            val featureName = cell.columnKey!!
            val property = config.get(categoryName, featureName, entry.enabled)
            properties.put(categoryName, featureName, property)
            if(!property.wasRead()) continue
            if(!property.isBooleanValue) property.set(entry.enabled)
            else entry.enabled = property.getBoolean(entry.enabled)
        }

        return ImmutableTable.copyOf(properties)
    }

    override fun getCategories(): Set<String> {
        return features.rowKeySet()
    }

    override fun getFeaturesInCategory(category: String): Set<String> {
        return features.row(category).keys
    }

    override fun isEnabled(category: String, name: String): Boolean {
        val result = features.get(category, name) ?: return false
        val rule = customRules.get(category, name)
        return rule?.isEnabled(result.enabled) ?: result.enabled
    }

    fun addCustomRule(category: String, name: String, rule: CustomFeatureRule) {
        val prev = customRules.put(category, name, rule)
        Preconditions.checkState(prev == null, "Duplicate rule on %s:%s", category, name)
    }
}