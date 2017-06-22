package org.generousg.fruitylib.network.event

import net.minecraftforge.fml.common.network.FMLEmbeddedChannel
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.Dispatcher
import org.generousg.fruitylib.network.ExtendedOutboundHandler


class NetworkEventDispatcher(registry: NetworkEventRegistry) : Dispatcher() {

    private val channels: Map<Side, FMLEmbeddedChannel>

    val senders: Senders

    init {
        this.channels = NetworkRegistry.INSTANCE.newChannel(CHANNEL_NAME, NetworkEventCodec(registry), NetworkEventInboundHandler())
        ExtendedOutboundHandler.install(this.channels)

        this.senders = Senders()
    }

    override fun getChannel(side: Side): FMLEmbeddedChannel {
        return channels[side]!!
    }

    companion object {

        val CHANNEL_NAME = "OpenMods|E"
    }

}