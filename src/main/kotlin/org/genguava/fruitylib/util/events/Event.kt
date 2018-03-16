package org.genguava.fruitylib.util.events

import java.util.*


open class Event<T> : ArrayList<(T)->Unit>() {
    operator fun plusAssign(element: (T)->Unit): Unit { add(element) }
    operator fun minusAssign(element: (T)->Unit): Unit { remove(element) }
    fun subscribe(listener: (T)->Unit) { add(listener) }
    fun unsubscribe(listener: (T)->Unit) { remove(listener) }
    fun fire(event: T) {
        this.forEach { it(event) }
    }
}
