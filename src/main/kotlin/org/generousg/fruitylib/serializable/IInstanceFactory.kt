package org.generousg.fruitylib.serializable


interface IInstanceFactory<T> {
    fun create(): T
}