package org.generousg.fruitylib.network

import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher
import net.minecraftforge.fml.relauncher.Side


interface IPacketTargetSelector<T> {
    fun isAllowedOnSide(side: Side): Boolean
    fun castArg(arg: Any): T
    fun listDispatchers(arg: T, result: MutableCollection<NetworkDispatcher>)
}