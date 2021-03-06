package org.generousg.fruitylib.config


abstract class AbstractFeatureManager {
    companion object Factory {
        val CATEGORY_ITEMS = "items"
        val CATEGORY_BLOCKS = "blocks"
        val CATEGORY_FLUIDS = "fluids"
    }
    abstract fun getCategories() : Set<String>
    abstract fun getFeaturesInCategory(category: String) : Set<String>
    abstract fun isEnabled(category: String, name: String): Boolean
    fun isBlockEnabled(name: String): Boolean = isEnabled(CATEGORY_BLOCKS, name)
    fun isItemEnabled(name: String): Boolean = isEnabled(CATEGORY_ITEMS, name)
    fun isFluidEnabled(name: String): Boolean = isEnabled(CATEGORY_FLUIDS, name)
}