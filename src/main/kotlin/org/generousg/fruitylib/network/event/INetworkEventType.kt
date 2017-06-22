package org.generousg.fruitylib.network.event

import network.event.EventDirection


interface INetworkEventType {
    fun createPacket(): NetworkEvent

    val direction: EventDirection

    val isCompressed: Boolean

    val isChunked: Boolean
}