package org.generousg.fruitylib.config


abstract class AbstractFeatureManager {
    companion object Factory {
        val CATEGORY_ITEMS = "items"
        val CATEGORY_BLOCKS = "blocks"
    }
    abstract fun getCategories() : Set<String>
    abstract fun getFeaturesInCategory(category: String) : Set<String>
    abstract fun isEnabled(category: String, name: String): Boolean
    fun isBlockEnabled(name: String): Boolean {
        return isEnabled(CATEGORY_BLOCKS, name)
    }
    fun isItemEnabled(name: String): Boolean {
        return isEnabled(CATEGORY_ITEMS, name)
    }
}