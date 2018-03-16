package org.generousg.fruitylib.util.io


interface IStringSerializer<out T> {
    fun readFromString(s: String): T
}