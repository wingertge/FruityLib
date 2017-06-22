package org.generousg.fruitylib.client.gui.logic

import com.google.common.collect.HashMultimap
import com.google.common.collect.Sets


class ValueUpdateDispatcher {
    private val actions = HashMultimap.create<Any, IValueUpdateAction>()

    fun addAction(listener: IValueUpdateAction) {
        for(trigger in listener.getTriggers())
            actions.put(trigger, listener)
    }

    fun trigger(triggers: Iterable<*>) {
        val actionsToTrigger = Sets.newIdentityHashSet<IValueUpdateAction>()

        for(trigger in triggers)
            actionsToTrigger.addAll(actions.get(trigger))

        for(action in actionsToTrigger)
            action.execute()
    }

    fun triggerAll() {
        val uniques = Sets.newIdentityHashSet<IValueUpdateAction>()
        uniques.addAll(actions.values())
        for(action in uniques) action.execute()
    }
}