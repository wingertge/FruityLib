package org.generousg.fruitylib.network.event

import network.event.EventDirection


@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkEventMeta(val compressed: Boolean = false, val chunked: Boolean = false, val direction: EventDirection = EventDirection.ANY)