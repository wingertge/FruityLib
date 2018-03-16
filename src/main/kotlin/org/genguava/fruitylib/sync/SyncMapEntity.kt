package org.genguava.fruitylib.sync

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import org.genguava.fruitylib.util.NetUtils


class SyncMapEntity<out H>(handler: H) : SyncMap<H>(handler) where H : Entity, H : ISyncMapProvider {

    override val handlerType: SyncMap.HandlerType = HandlerType.ENTITY

    override val playersWatching: Set<EntityPlayerMP> get() = NetUtils.getPlayersWatchingEntity(handler.world as WorldServer, handler.entityId)

    override val world: World = handler.world

    override val invalid = handler.isDead
}
