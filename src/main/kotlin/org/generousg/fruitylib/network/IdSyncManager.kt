package org.generousg.fruitylib.network

import com.google.common.io.Closer
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.minecraft.network.PacketBuffer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import net.minecraftforge.fml.common.network.NetworkHandshakeEstablished
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import org.generousg.fruitylib.FruityLib
import org.generousg.fruitylib.datastore.DataStoreBuilder
import org.generousg.fruitylib.datastore.DataStoreKey
import org.generousg.fruitylib.datastore.DataStoreManager
import org.generousg.fruitylib.datastore.DataStoreWriter
import org.generousg.fruitylib.util.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream


class IdSyncManager private constructor() : DataStoreManager() {
    private object InstanceHolder { val INSTANCE = IdSyncManager() }
    companion object {
        val instance = lazy { InstanceHolder.INSTANCE }
        private val CHANNEL_NAME = "FruityLib|I"
        private fun serializeToPacket(key: DataStoreKey<*, *>, writer: DataStoreWriter<*, *>): FMLProxyPacket {
            val payload = Unpooled.buffer()
            val closer = Closer.create()

            try {
                closer.use { closer1 ->
                    val raw = closer1.register(ByteBufOutputStream(payload))
                    val compressed = closer1.register(GZIPOutputStream(raw))
                    val output = DataOutputStream(compressed)
                    output.writeUTF(key.id)
                    writer.write(output)
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            return FMLProxyPacket(PacketBuffer(payload.copy()), CHANNEL_NAME)
        }
    }

    init {
        NetworkRegistry.INSTANCE.newChannel(CHANNEL_NAME, InboundHandler())
    }

    @ChannelHandler.Sharable
    internal inner class InboundHandler : SimpleChannelInboundHandler<FMLProxyPacket>() {
        override fun channelRead0(ctx: ChannelHandlerContext, msg: FMLProxyPacket) {
            val buf = msg.payload()

            try {
                decodeIds(buf)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        @Throws(Exception::class)
        override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
            if(evt is NetworkHandshakeEstablished) {
                Log.debug {"Sending id data for player: ${FruityLib.proxy.getPlayerFromHandler(evt.netHandler)}"}
                sendAllIds(ctx)
            } else {
                ctx.fireUserEventTriggered(evt)
            }
        }
    }

    fun <K, V> createDataStore(domain: String, id: String, keyClass: Class<out K>, valueClass: Class<out V>) = createDataStore("$domain:$id", keyClass, valueClass)
    override fun <K, V> createDataStore(id: String, keyClass: Class<out K>, valueClass: Class<out V>): DataStoreBuilder<K, V> {
        require(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) { "This method cannot be called in post-initialization neighborState and later" }
        return super.createDataStore(id, keyClass, valueClass)
    }

    private fun sendAllIds(ctx: ChannelHandlerContext) {
        validate()

        for((key, value) in dataStoreMeta.entries) {
            val packet = serializeToPacket(key, value.createWriter())
            ctx.write(packet)
        }
    }

    @Throws(IOException::class)
    private fun decodeIds(buf: ByteBuf) {
        val closer = Closer.create()
        closer.use { closer1 ->
            val raw = closer1.register(ByteBufInputStream(buf))
            val compressed = closer1.register(GZIPInputStream(raw))
            val input = DataInputStream(compressed)

            val keyId = input.readUTF()

            Log.debug {"Received data store for key $keyId, packet size = ${buf.writerIndex()}"}
            val wrapper = getDataStoreMeta<Any?, Any?>(keyId)
            val reader = wrapper.createReader()
            reader.read(input)
        }
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        Log.debug {"Disconnected, restoring local data"}
        activateLocalData()
    }

    fun finishLoading() {
        validate()
        MinecraftForge.EVENT_BUS.register(this)
    }
}
