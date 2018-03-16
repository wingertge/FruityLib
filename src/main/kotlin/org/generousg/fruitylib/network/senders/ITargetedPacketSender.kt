package org.generousg.fruitylib.network.senders


interface ITargetedPacketSender<T> {
    fun sendMessage(msg: Any?, target: T)
    fun sendMessages(msg: Collection<Any?>, target: T)
    fun bind(target: T): IPacketSender
}