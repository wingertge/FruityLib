package org.generousg.fruitylib.sync


interface ISyncableValueProvider<out T> : ISyncableObject {
    val value: T
}