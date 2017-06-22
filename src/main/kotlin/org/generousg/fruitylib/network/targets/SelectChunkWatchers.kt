package org.generousg.fruitylib.network.targets

import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.DimCoord
import org.generousg.fruitylib.network.IPacketTargetSelector
import org.generousg.fruitylib.util.NetUtils


class SelectChunkWatchers : IPacketTargetSelector<DimCoord> {
    object InstanceHolder { val INSTANCE = SelectChunkWatchers() }
    companion object { val instance = lazy { InstanceHolder.INSTANCE } }

    override fun isAllowedOnSide(side: Side): Boolean = side == Side.SERVER
    override fun listDispatchers(arg: DimCoord, result: MutableCollection<NetworkDispatcher?>) {
        val server = DimensionManager.getWorld(arg.dimension)
        val players = NetUtils.getPlayersWatchingBlock(server, arg.x, arg.z)
        players.mapTo(result) { NetUtils.getPlayerDispatcher(it) }
    }

    override fun castArg(arg: Any): DimCoord = arg as DimCoord
}