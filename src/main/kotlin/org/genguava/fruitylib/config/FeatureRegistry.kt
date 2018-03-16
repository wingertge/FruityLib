package org.genguava.fruitylib.config


import com.google.common.collect.ImmutableTable
import com.google.common.collect.Maps
import com.google.common.collect.Table
import net.minecraftforge.common.config.Property
import net.minecraftforge.fml.common.Loader



class FeatureRegistry private constructor(){
    private object Holder { val INSTANCE = FeatureRegistry() }
    companion object { val instance: FeatureRegistry by lazy { Holder.INSTANCE } }

    private class Entry(val manager: AbstractFeatureManager, val properties: Table<String, String, Property>)
    private var features = Maps.newHashMap<String, Entry>()

    private fun addValue(entry: Entry) {
        val mod = Loader.instance().activeModContainer()
        requireNotNull(mod) { "Can't register outside initialization"}
        val modId = mod!!.modId
        val prev = features.put(modId, entry)
        require(prev == null) { "Duplicate on modid: $modId" }
    }

    fun register(manager: AbstractFeatureManager) = addValue(Entry(manager, ImmutableTable.of()))
    fun register(manager: AbstractFeatureManager, properties: Table<String, String, Property>) {
        requireNotNull(properties)
        addValue(Entry(manager, ImmutableTable.copyOf(properties)))
    }

    fun getManager(modId: String): AbstractFeatureManager? {
        val entry = features[modId]
        return entry?.manager
    }

    fun isEnabled(modId: String, category: String, feature: String): Boolean {
        val entry = features[modId]
        return entry?.manager?.isEnabled(category, feature) ?: false
    }

    fun getProperty(modId: String, category: String, feature: String): Property? {
        val entry = features[modId]
        return entry?.properties?.get(category, feature)
    }
}