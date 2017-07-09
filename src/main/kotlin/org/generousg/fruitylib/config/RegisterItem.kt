package org.generousg.fruitylib.config


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterItem(val name: String, val unlocalizedName: String = "[default]", val modelName: String = "[default]", val enabled: Boolean = true, val configurable: Boolean = true,
                              val creativeTab: String = "[default]", val hasInfo: Boolean = false)
