package org.generousg.fruitylib.inventory


open class NamedItemHandler(override var name: String, size: Int) : GenericItemHandler(size), INamedItemHandler {
}