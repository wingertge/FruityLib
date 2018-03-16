package org.generousg.fruitylib.sync

import com.google.common.collect.Maps
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.ExtendedOutboundHandler
import org.generousg.fruitylib.network.senders.ExtPacketSenderFactory
import org.generousg.fruitylib.network.senders.ITargetedPacketSender


class SyncChannelHolder private constructor() {
    object InstanceHolder { val INSTANCE = SyncChannelHolder() }
    companion object {
        val CHANNEL_NAME = "FruityLib|M"
        val instance = lazy { InstanceHolder.INSTANCE }

        fun createPacket(payload: PacketBuffer) = FMLProxyPacket(payload, CHANNEL_NAME)
        fun ensureLoaded() = Unit
    }

    private val senders = Maps.newEnumMap<Side, ITargetedPacketSender<Collection<EntityPlayerMP>>>(Side::class.java)

    init {
        val channels = NetworkRegistry.INSTANCE.newChannel(CHANNEL_NAME, InboundSyncHandler())

        for((key, channel) in channels) {
            ExtendedOutboundHandler.install(channel)
            senders[key] = ExtPacketSenderFactory.createMultiplePlayersSender(channel)
        }
    }

    fun sendPayloadToPlayers(payload: PacketBuffer, players: Collection<EntityPlayerMP>) {
        val packet = FMLProxyPacket(payload, CHANNEL_NAME)
        senders[Side.SERVER]?.sendMessage(packet, players)
    }
}