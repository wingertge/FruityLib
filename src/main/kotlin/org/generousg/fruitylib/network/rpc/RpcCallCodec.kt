package org.generousg.fruitylib.network.rpc

import com.google.common.base.Preconditions
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import org.generousg.fruitylib.FruityLib
import org.generousg.fruitylib.util.ByteUtils
import java.lang.reflect.Method

@ChannelHandler.Sharable
class RpcCallCodec(private val targetRegistry: TargetWrapperRegistry, private val methodRegistry: MethodIdRegistry) : MessageToMessageCodec<FMLProxyPacket, RpcCall>() {
    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, call: RpcCall, out: MutableList<Any>) {
        val buf = Unpooled.buffer()

        val output = ByteBufOutputStream(buf)

        run {
            val targetWrapper = call.target
            val targetId = targetRegistry.getWrapperId(targetWrapper.javaClass)
            ByteUtils.writeVLI(output, targetId)
            targetWrapper.writeToStream(output)
        }

        run {
            val method = call.method
            val methodId = methodRegistry.methodToId(method)
            ByteUtils.writeVLI(output, methodId)
            val paramsCodec = MethodParamsCodec.create(method)
            paramsCodec.writeArgs(output, *call.args)
        }

        val packet = FMLProxyPacket(PacketBuffer(buf.copy()), RpcCallDispatcher.CHANNEL_NAME)
        out.add(packet)
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: FMLProxyPacket, out: MutableList<Any>) {
        val input = ByteBufInputStream(msg.payload())

        var target: IRpcTarget? = null
        var method: Method? = null
        var args: Array<Any?>? = null

        run {
            val targetId = ByteUtils.readVLI(input)
            target = targetRegistry.createWrapperFromId(targetId)
            val player = getPlayer(msg)
            target!!.readFromStream(player, input)
        }

        run {
            val methodId = ByteUtils.readVLI(input)
            method = methodRegistry.idToMethod(methodId)
            val paramsCodec = MethodParamsCodec.create(method!!)
            args = paramsCodec.readArgs(input)
        }

        val bufferJunkSize = input.available()
        Preconditions.checkState(bufferJunkSize == 0, "%s junk bytes left in buffer, method = %s", bufferJunkSize, method)

        out.add(RpcCall(target!!, method!!, args!!))
    }

    protected fun getPlayer(msg: FMLProxyPacket): EntityPlayer {
        val handler = msg.handler()
        val player = FruityLib.proxy.getPlayerFromHandler(handler)
        Preconditions.checkNotNull(player, "Can't get player from handler %s", handler)
        return player!!
    }
}
