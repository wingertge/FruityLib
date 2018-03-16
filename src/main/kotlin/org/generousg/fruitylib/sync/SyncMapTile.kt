package org.generousg.fruitylib.sync

import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import org.generousg.fruitylib.util.NetUtils


open class SyncMapTile<out H>(handler: H) : SyncMap<H>(handler) where H : TileEntity, H : ISyncMapProvider {
    override val handlerType: SyncMap.HandlerType
        get() = SyncMap.HandlerType.TILE_ENTITY

    override val playersWatching: Set<EntityPlayerMP>
        get() = NetUtils.getPlayersWatchingBlock(handler.world as WorldServer, handler.pos.x, handler.pos.z).toSet()

    override val world: World
        get() = handler.world

    override val invalid: Boolean
        get() = handler.isInvalid
}