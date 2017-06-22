package org.generousg.fruitylib.network

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.netty.util.AttributeKey
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel
import net.minecraftforge.fml.common.network.FMLOutboundHandler
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import net.minecraftforge.fml.relauncher.Side


class ExtendedOutboundHandler : ChannelOutboundHandlerAdapter() {
    companion object {
        val MESSAGETARGET = AttributeKey.valueOf<IPacketTargetSelector<*>>("fl:outboundTarget")

        private fun <T> getDispatchers(target: IPacketTargetSelector<T>, arg: Any): Collection<NetworkDispatcher> {
            val output = arrayListOf<NetworkDispatcher>()
            target.listDispatchers(target.castArg(arg), output.toMutableList())
            return output
        }

        fun install(channels: Map<Side, FMLEmbeddedChannel>) {
            for(side in Side.values())
                install(channels[side])
        }

        fun install(fmlEmbeddedChannel: FMLEmbeddedChannel?) = fmlEmbeddedChannel?.pipeline()?.addAfter("fml:outbound", "fl:outbound", ExtendedOutboundHandler())
    }

    @Throws(Exception::class)
    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise) {
        if(msg !is FMLProxyPacket) {
            ctx.write(msg)
            return
        }

        val channel = ctx.channel()

        val target = channel.attr(MESSAGETARGET).get()
        if(target == null) {
            ctx.write(msg)
            return
        }

        val pkt = msg
        val channelSide = channel.attr(NetworkRegistry.CHANNEL_SOURCE).get()
        require(target.isAllowedOnSide(channelSide)) { "Packet is not allowed on side" }
        val channelName = channel.attr(NetworkRegistry.FML_CHANNEL).get()
        val arg = channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).get()

        try {
            val dispatchers = getDispatchers(target, arg)
            for (dispatcher in dispatchers)
                dispatcher.sendProxy(pkt)
        } catch (t: Throwable) {
            throw IllegalStateException("Failed to select and send message (selector $target, arg: $arg, channel: $channelName, side: $channelSide)", t)
        }
    }
}