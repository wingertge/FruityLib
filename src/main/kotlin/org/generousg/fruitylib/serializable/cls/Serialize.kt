package org.generousg.fruitylib.serializable.cls

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Serialize(val rank: Int, val nullable: Boolean = true)