package org.generousg.fruitylib.config

import com.google.common.base.Preconditions
import com.google.common.base.Throwables
import com.google.common.collect.Maps
import net.minecraft.block.Block


open class FactoryRegistry<T : Any> {
    interface Factory<out T> {
        fun construct(): T
    }

    private val customFactories = Maps.newHashMap<String, Factory<T>>()
    private val customItemBlockFactories = Maps.newHashMap<String, (Block) -> T>()

    fun registerFactory(feature: String, factory: Factory<T>) = customFactories.put(feature, factory)
    fun registerItemBlockFactory(feature: String, factory: (Block) -> T) = customItemBlockFactories.put(feature, factory)

    fun <C: T> construct(feature: String, clazz: Class<out C>) : C {
        val customFactory = customFactories[feature]
        if(customFactory != null) {
            @Suppress("UNCHECKED_CAST")
            val result = customFactory.construct() as? C
            Preconditions.checkArgument(clazz.isInstance(result),
                    "Invalid class for feature entry '$feature', got '${result?.javaClass ?: "null"}', expected '$clazz'")
        }

        try {
            return clazz.newInstance()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun <C: T> constructItemBlock(feature: String, clazz: Class<out C>, block: Block) : C {
        val customFactory = customItemBlockFactories[feature]
        if(customFactory != null) {
            @Suppress("UNCHECKED_CAST")
            val result = customFactory.invoke(block) as? C
            Preconditions.checkArgument(clazz.isInstance(result),
                    "Invalid class for feature entry '$feature', got '${result?.javaClass ?: "null"}', expected '$clazz'")
        }

        try {
            return clazz.getDeclaredConstructor(Block::class.java).newInstance(block)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}