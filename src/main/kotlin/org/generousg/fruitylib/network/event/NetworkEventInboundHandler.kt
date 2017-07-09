package org.generousg.fruitylib.network.event

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.network.FMLOutboundHandler


@Sharable
class NetworkEventInboundHandler : SimpleChannelInboundHandler<NetworkEvent>() {

    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: NetworkEvent) {
        MinecraftForge.EVENT_BUS.post(msg)
        msg.dispatcher = null

        for (reply in msg.replies) {
            ctx.channel().attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.REPLY)
            ctx.writeAndFlush(reply)
        }
    }

}