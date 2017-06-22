package org.generousg.fruitylib.config


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ConfigProperty(val name: String = "", val category: String, val comment: String = "")