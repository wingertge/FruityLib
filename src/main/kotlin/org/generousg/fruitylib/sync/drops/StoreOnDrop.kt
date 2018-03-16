package org.generousg.fruitylib.sync.drops

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class StoreOnDrop(val name: String = "")