package org.generousg.fruitylib.client.gui.logic


interface IValueUpdateAction {
    fun getTriggers(): Iterable<*>
    fun execute()
}