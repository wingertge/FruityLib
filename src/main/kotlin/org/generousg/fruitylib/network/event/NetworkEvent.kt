package org.generousg.fruitylib.network.event

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException


@Suppress("unused", "UNUSED_PARAMETER")
abstract class NetworkEvent : Event() {

    internal val replies: MutableList<NetworkEvent> = Lists.newArrayList<NetworkEvent>()

    internal var dispatcher: NetworkDispatcher? = null

    var sender: EntityPlayer? = null

    @Throws(IOException::class)
    abstract fun readFromStream(input: DataInput)

    @Throws(IOException::class)
    abstract fun writeToStream(output: DataOutput)

    protected fun appendLogInfo(info: List<String>) {}

    fun reply(reply: NetworkEvent) {
        Preconditions.checkState(dispatcher != null, "Can't call this method outside event handler")
        reply.dispatcher = dispatcher
        this.replies.add(reply)
    }

    fun sendToAll() {
        NetworkEventManager.instance.dispatcher().senders.global.sendMessage(this)
    }

    fun sendToServer() {
        NetworkEventManager.instance.dispatcher().senders.client.sendMessage(this)
    }

    fun sendToPlayer(player: EntityPlayer) {
        NetworkEventManager.instance.dispatcher().senders.player.sendMessage(this, player)
    }

    fun sendToEntity(entity: Entity) {
        NetworkEventManager.instance.dispatcher().senders.entity.sendMessage(this, entity)
    }

    fun serialize(): List<Any?> {
        return NetworkEventManager.instance.dispatcher().senders.serialize(this)
    }
}
