package org.generousg.fruitylib.network.rpc

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

@ChannelHandler.Sharable
class RpcCallInboundHandler : SimpleChannelInboundHandler<RpcCall>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: RpcCall) {
        val target = msg.target.target
        requireNotNull(target) { "Target wrapper ${msg.target} returned null object" }
        msg.method.invoke(target, msg.args)
        msg.target.afterCall()
    }
}