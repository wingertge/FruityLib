package org.generousg.fruitylib.network.targets

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.IPacketTargetSelector
import org.generousg.fruitylib.util.Log
import org.generousg.fruitylib.util.NetUtils


class SelectMultiplePlayers : IPacketTargetSelector<Collection<EntityPlayerMP>> {
    object InstanceHolder { val INSTANCE = SelectMultiplePlayers() }
    companion object { val instance = lazy { InstanceHolder.INSTANCE } }

    @Suppress("UNCHECKED_CAST")
    override fun castArg(arg: Any): Collection<EntityPlayerMP> = arg as Collection<EntityPlayerMP>
    override fun isAllowedOnSide(side: Side): Boolean = side == Side.SERVER

    override fun listDispatchers(arg: Collection<EntityPlayerMP>, result: MutableCollection<NetworkDispatcher>) {
        for(player in arg) {
            val dispatcher = NetUtils.getPlayerDispatcher(player)
            if(dispatcher != null) result.add(dispatcher)
            else Log.info {"Trying to send message to disconnected player $player"}
        }
    }
}