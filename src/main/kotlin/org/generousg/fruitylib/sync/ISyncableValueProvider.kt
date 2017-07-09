package org.generousg.fruitylib.sync

import kotlin.reflect.KProperty


interface ISyncableValueProvider<T> : ISyncableObject {
    val value: T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}
