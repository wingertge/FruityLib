package org.generousg.fruitylib.network.senders

import io.netty.channel.Channel
import org.generousg.fruitylib.util.NetUtils


open class TargetedPacketSenderBase<T>(private val channel: Channel) : ITargetedPacketSender<T> {
    protected open fun configureChannel(channel: Channel, target: T) = Unit
    protected open fun cleanupChannel(channel: Channel) = Unit

    override fun sendMessage(msg: Any?, target: T) {
        configureChannel(channel, target)
        channel.writeAndFlush(msg).addListener(NetUtils.LOGGING_LISTENER)
        cleanupChannel(channel)
    }

    override fun sendMessages(msg: Collection<Any?>, target: T) {
        configureChannel(channel, target)

        for(msg1 in msg) channel.write(msg1).addListener(NetUtils.LOGGING_LISTENER)

        channel.flush()
        cleanupChannel(channel)
    }

    override fun bind(target: T): IPacketSender {
        return object: IPacketSender {
            override fun sendMessage(msg: Any?) {
                sendMessage(msg, target)
            }

            override fun sendMessages(msg: Collection<Any?>) {
                sendMessages(msg, target)
            }
        }
    }
}