package org.generousg.fruitylib.network.event


interface INetworkEventType {
    fun createPacket(): NetworkEvent

    val direction: EventDirection

    val isCompressed: Boolean

    val isChunked: Boolean
}