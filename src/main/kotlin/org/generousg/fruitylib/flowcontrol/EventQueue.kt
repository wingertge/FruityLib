package org.generousg.fruitylib.flowcontrol

import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent


class EventQueue {
    enum class EventType { PRE_INIT, INIT, POST_INIT }

    companion object {
        private val preInitEvents = mutableListOf<(event: FMLPreInitializationEvent) -> Unit>()
        private val initEvents = mutableListOf<(event: FMLInitializationEvent) -> Unit>()
        private val postInitEvents = mutableListOf<(event: FMLPostInitializationEvent) -> Unit>()

        fun queueActionForPreInit(action: (event:FMLPreInitializationEvent) -> Unit) = preInitEvents.add(action)
        fun queueActionForInit(action: (event: FMLInitializationEvent) -> Unit) = initEvents.add(action)
        fun queueActionForPostInit(action: (event: FMLPostInitializationEvent) -> Unit) = postInitEvents.add(action)
    }

    fun preInit(event: FMLPreInitializationEvent) {
        for (action in preInitEvents) action.invoke(event)
        preInitEvents.clear()
    }

    fun init(event: FMLInitializationEvent) {
        for (action in initEvents) action.invoke(event)
        initEvents.clear()
    }

    fun postInit(event: FMLPostInitializationEvent) {
        for(action in postInitEvents) action.invoke(event)
        postInitEvents.clear()
    }
}