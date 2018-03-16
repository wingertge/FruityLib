package org.generousg.fruitylib.network.senders

import io.netty.channel.Channel
import org.generousg.fruitylib.util.NetUtils


open class PacketSenderBase(private val channel: Channel) : IPacketSender {
    protected open fun configureChannel(channel: Channel) = Unit
    protected open fun cleanupChannel(channel: Channel) = Unit

    override fun sendMessage(msg: Any?) {
        configureChannel(channel)
        channel.writeAndFlush(msg)
        cleanupChannel(channel)
    }

    override fun sendMessages(msg: Collection<Any?>) {
        configureChannel(channel)
        for(msg1 in msg) channel.write(msg1).addListener(NetUtils.LOGGING_LISTENER)
        channel.flush()
        cleanupChannel(channel)
    }
}