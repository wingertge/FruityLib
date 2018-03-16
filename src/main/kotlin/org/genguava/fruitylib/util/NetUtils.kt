package org.genguava.fruitylib.util

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import io.netty.channel.ChannelFutureListener
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import java.lang.reflect.Field


class NetUtils {
    companion object {
        val LOGGING_LISTENER = ChannelFutureListener {
            if(!it.isSuccess) {
                val cause = it.cause()
                Log.severe(cause, "Crash in pipeline handler")
            }
        }

        private var trackingPlayers: Field? = null

        fun getPlayersWatchingBlock(world: WorldServer, blockX: Int, blockZ: Int) = getPlayersWatchingChunk(world, blockX shr 4, blockZ shr 4)
        fun getPlayersWatchingChunk(world: WorldServer, chunkX: Int, chunkZ: Int): List<EntityPlayerMP> {
            return ImmutableList.copyOf(world.playerChunkMap.getEntry(chunkX, chunkZ)?.players ?: arrayListOf())
        }

        fun getPlayerDispatcher(player: EntityPlayerMP): NetworkDispatcher? = player.connection.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get()
        fun getPlayersWatchingEntity(server: WorldServer, entityId: Int): Set<EntityPlayerMP> {
            val tracker = server.entityTracker
            val entry = tracker.trackedEntityHashTable.lookup(entityId)
            return entry?.trackingPlayers?.toSet() ?: setOf()
        }
    }
}