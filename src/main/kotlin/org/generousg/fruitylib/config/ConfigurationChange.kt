package org.generousg.fruitylib.config

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event


open class ConfigurationChange(val name: String, val category: String) : Event() {
    fun check(category: String, name: String): Boolean = this.category == category && this.name == name

    @Cancelable
    class Pre(name: String, category: String, val proposedValues: Array<String>) : ConfigurationChange(name, category)

    class Post(name: String, category: String) : ConfigurationChange(name, category)
}