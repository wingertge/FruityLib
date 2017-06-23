package org.generousg.fruitylib.network.targets

import net.minecraft.entity.Entity
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.IPacketTargetSelector
import org.generousg.fruitylib.util.NetUtils


class SelectEntityWatchers : IPacketTargetSelector<Entity> {
    object InstanceHolder { val INSTANCE = SelectEntityWatchers() }
    companion object { val instance = lazy { InstanceHolder.INSTANCE } }

    override fun isAllowedOnSide(side: Side): Boolean = side == Side.SERVER
    override fun listDispatchers(arg: Entity, result: MutableCollection<NetworkDispatcher>) {
        require(arg.world is WorldServer) { "Invalid side" }

        val players = NetUtils.getPlayersWatchingEntity(arg.world as WorldServer, arg.entityId)
        players.mapTo(result) { NetUtils.getPlayerDispatcher(it)!! }
    }

    override fun castArg(arg: Any): Entity = arg as Entity
}