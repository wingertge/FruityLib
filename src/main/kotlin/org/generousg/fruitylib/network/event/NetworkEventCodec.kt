package org.generousg.fruitylib.network.event

import com.google.common.base.Preconditions
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageCodec
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.ByteBufUtils
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.FruityLib
import util.io.PacketChunker
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


@Sharable
class NetworkEventCodec(private val registry: NetworkEventRegistry) : MessageToMessageCodec<FMLProxyPacket, NetworkEvent>() {

    private val chunker = PacketChunker()

    @Throws(IOException::class)
    override fun encode(ctx: ChannelHandlerContext, msg: NetworkEvent, out: MutableList<Any>) {
        val id = registry.getIdForClass(msg.javaClass)
        val type = registry.getTypeForId(id)

        val payload = toRawBytes(msg, type.isCompressed)
        val channel = ctx.channel()

        val side = channel.attr(NetworkRegistry.CHANNEL_SOURCE).get()
        val validator = type.direction
        Preconditions.checkState(validator.validateSend(side),
                "Invalid direction: sending packet %s on side %s", msg.javaClass, side)

        if (type.isChunked) {
            val maxChunkSize = if (side === Side.SERVER) PacketChunker.PACKET_SIZE_S3F else PacketChunker.PACKET_SIZE_C17
            val chunked = chunker.splitIntoChunks(payload, maxChunkSize)
            for (chunk in chunked) {
                val partialPacket = createPacket(id, chunk)
                partialPacket.dispatcher = msg.dispatcher
                out.add(partialPacket)
            }
        } else {
            val partialPacket = createPacket(id, payload)
            partialPacket.dispatcher = msg.dispatcher
            out.add(partialPacket)
        }
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, msg: FMLProxyPacket, out: MutableList<Any>) {
        val payload = msg.payload()
        val typeId = ByteBufUtils.readVarInt(payload, 5)
        val type = registry.getTypeForId(typeId)

        val channel = ctx.channel()

        val side = channel.attr(NetworkRegistry.CHANNEL_SOURCE).get()

        val validator = type.direction
        Preconditions.checkState(validator.validateReceive(side),
                "Invalid direction: receiving packet %s on side %s", msg.javaClass, side)

        var input: InputStream = ByteBufInputStream(payload)

        if (type.isChunked) {
            val fullPayload = chunker.consumeChunk(input, input.available()) ?: return
            input = ByteArrayInputStream(fullPayload)
        }

        if (type.isCompressed) input = GZIPInputStream(input)

        val data = DataInputStream(input)

        val event = type.createPacket()
        event.readFromStream(data)
        event.dispatcher = msg.dispatcher

        val handler = msg.handler()
        if (handler != null) event.sender = FruityLib.proxy!!.getPlayerFromHandler(handler)

        val bufferJunkSize = input.available()
        if (bufferJunkSize > 0) {
            // compressed stream neads extra read to realize it's finished
            Preconditions.checkState(input.read() == -1, "%s junk bytes left in buffer, event", bufferJunkSize, event)
        }
        input.close()

        out.add(event)
    }

    private fun createPacket(id: Int, payload: ByteArray): FMLProxyPacket {
        val buf = Unpooled.buffer(payload.size + 5)
        ByteBufUtils.writeVarInt(buf, id, 5)
        buf.writeBytes(payload)
        val partialPacket = FMLProxyPacket(PacketBuffer(buf.copy()), NetworkEventDispatcher.CHANNEL_NAME)
        return partialPacket
    }

    @Throws(IOException::class)
    private fun toRawBytes(event: NetworkEvent, compress: Boolean): ByteArray {
        val payload = ByteArrayOutputStream()

        val stream = if (compress) GZIPOutputStream(payload) else payload
        val output = DataOutputStream(stream)
        event.writeToStream(output)
        stream.close()

        return payload.toByteArray()
    }
}