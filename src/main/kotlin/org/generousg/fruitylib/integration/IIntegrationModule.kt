package org.generousg.fruitylib.integration


interface IIntegrationModule {
    val canLoad: Boolean
    val name: String
    fun load()
}