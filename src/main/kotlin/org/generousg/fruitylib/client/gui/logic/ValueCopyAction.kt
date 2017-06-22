package org.generousg.fruitylib.client.gui.logic

import com.google.common.collect.ImmutableList
import org.generousg.fruitylib.util.events.ValueChangedEvent


class ValueCopyAction<T>(private val trigger: Any, private val provider: ()->ValueChangedEvent<T>, private val receiver: (ValueChangedEvent<T>)->Unit) : IValueUpdateAction {

    override fun getTriggers(): Iterable<*> {
        return ImmutableList.of(trigger)
    }

    override fun execute() {
        val value = provider.invoke()
        receiver.invoke(value)
    }

    companion object {

        fun <T> create(provider: ()->ValueChangedEvent<T>, receiver: (ValueChangedEvent<T>)->Unit): ValueCopyAction<T> {
            return ValueCopyAction(provider, provider, receiver)
        }

        fun <T> create(trigger: Any, provider: ()->ValueChangedEvent<T>, receiver: (ValueChangedEvent<T>)->Unit): ValueCopyAction<T> {
            return ValueCopyAction(trigger, provider, receiver)
        }
    }
}