package org.generousg.fruitylib.network.senders

import io.netty.channel.Channel
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.network.FMLOutboundHandler
import net.minecraftforge.fml.common.network.FMLOutboundHandler.OutboundTarget
import net.minecraftforge.fml.common.network.NetworkRegistry


class FmlPacketSenderFactory {
    companion object Factory {
        private class FmlPacketSender(channel: Channel, private val selector: OutboundTarget) : PacketSenderBase(channel) {
            override fun configureChannel(channel: Channel) = channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(selector)
        }

        class FmlTargetedPacketSender<T>(channel: Channel, private val selector: OutboundTarget) : TargetedPacketSenderBase<T>(channel) {
            override fun configureChannel(channel: Channel, target: T) {
                channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(selector)
                channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(target)
            }
        }

        fun createPlayerSender(channel: Channel) = FmlTargetedPacketSender<EntityPlayer>(channel, OutboundTarget.PLAYER)
        fun createDimensionSender(channel: Channel) = FmlTargetedPacketSender<Int>(channel, OutboundTarget.DIMENSION)
        fun createPointSender(channel: Channel) = FmlTargetedPacketSender<NetworkRegistry.TargetPoint>(channel, OutboundTarget.ALLAROUNDPOINT)
        fun createSender(channel: Channel, target: OutboundTarget): IPacketSender = FmlPacketSender(channel, target)
    }
}