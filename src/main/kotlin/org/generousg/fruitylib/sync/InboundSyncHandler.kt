package org.generousg.fruitylib.sync

import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import org.generousg.fruitylib.FruityLib
import java.io.DataInputStream

@ChannelHandler.Sharable
class InboundSyncHandler : SimpleChannelInboundHandler<FMLProxyPacket>() {
    companion object {
        class SyncException(cause: Throwable, provider: ISyncMapProvider) : RuntimeException("Failed to sync $provider (${provider.javaClass})", cause)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: FMLProxyPacket) {
        val world = FruityLib.proxy.clientWorld

        val payload = msg.payload()
        val input = DataInputStream(ByteBufInputStream(payload))

        val provider = SyncMap.findSyncMap(world!!, input)
        try {
            provider?.syncMap?.readFromStream(input)
        } catch (e: Throwable) {
            throw SyncException(e, provider!!)
        }
    }
}
