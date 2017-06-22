package org.generousg.fruitylib.network

import com.google.common.collect.ImmutableList
import io.netty.channel.embedded.EmbeddedChannel
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.senders.ExtPacketSenderFactory
import org.generousg.fruitylib.network.senders.FmlPacketSenderFactory


abstract class Dispatcher {
    protected abstract fun getChannel(side: Side): EmbeddedChannel
    open protected val serverChannel get() = getChannel(Side.SERVER)
    open protected val clientChannel get() = getChannel(Side.CLIENT)

    inner class Senders {
        val client = FmlPacketSenderFactory.createSender(clientChannel, OutboundTarget.ALLAROUNDPOINT)
        val global = FmlPacketSenderFactory.createSender(serverChannel, OutboundTarget.ALL)
        val nowhere = FmlPacketSenderFactory.createSender(serverChannel, OutboundTarget.NOWHERE)
        val player = FmlPacketSenderFactory.createPlayerSender(serverChannel)
        val dimension = FmlPacketSenderFactory.createDimensionSender(serverChannel)
        val point = FmlPacketSenderFactory.createPointSender(serverChannel)
        val block = ExtPacketSenderFactory.createBlockSender(serverChannel)
        val entity = ExtPacketSenderFactory.createEntitySender(serverChannel)

        fun serialize(msg: Any?): List<Any?> {
            nowhere.sendMessage(msg)

            val result = ImmutableList.builder<Any?>()
            var packet: Any? = null
            while (serverChannel.outboundMessages().peek() != null) {
                packet = serverChannel.outboundMessages().poll()
                result.add(packet)
            }

            return result.build()
        }
    }
}