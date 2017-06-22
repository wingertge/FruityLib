package org.generousg.fruitylib.network.senders


interface IPacketSender {
    fun sendMessage(msg: Any?)
    fun sendMessages(msg: Collection<Any?>)
}