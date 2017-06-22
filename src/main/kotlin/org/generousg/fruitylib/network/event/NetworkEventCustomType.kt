package org.generousg.fruitylib.network.event

import kotlin.reflect.KClass


@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkEventCustomType(val value: KClass<out INetworkEventType>)