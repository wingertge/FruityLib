package org.generousg.fruitylib.network.senders

import io.netty.channel.Channel
import net.minecraftforge.fml.common.network.FMLOutboundHandler
import org.generousg.fruitylib.network.ExtendedOutboundHandler
import org.generousg.fruitylib.network.IPacketTargetSelector
import org.generousg.fruitylib.network.targets.SelectChunkWatchers
import org.generousg.fruitylib.network.targets.SelectEntityWatchers
import org.generousg.fruitylib.network.targets.SelectMultiplePlayers


class ExtPacketSenderFactory {
    companion object {
        class ExtTargetedPacketSender<T>(channel: Channel, val selector: IPacketTargetSelector<T>) : TargetedPacketSenderBase<T>(channel) {
            override fun configureChannel(channel: Channel, target: T) {
                channel.attr(ExtendedOutboundHandler.MESSAGETARGET).set(selector)
                channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(target)
            }

            override fun cleanupChannel(channel: Channel) = channel.attr(ExtendedOutboundHandler.MESSAGETARGET).set(null)
        }

        fun <T> createSender(channel: Channel, selector: IPacketTargetSelector<T>) = ExtTargetedPacketSender(channel, selector)
        fun createBlockSender(channel: Channel) = createSender(channel, SelectChunkWatchers.instance.value)
        fun createEntitySender(channel: Channel) = createSender(channel, SelectEntityWatchers.instance.value)
        fun createMultiplePlayersSender(channel: Channel) = createSender(channel, SelectMultiplePlayers.instance.value)
    }
}