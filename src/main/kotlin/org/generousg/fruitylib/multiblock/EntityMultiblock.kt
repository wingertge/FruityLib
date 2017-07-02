package org.generousg.fruitylib.multiblock

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.generousg.fruitylib.sync.SyncedEntity


abstract class EntityMultiblock(world: World) : SyncedEntity(world) {
    abstract fun destroy()
    abstract fun rebuild(pos: BlockPos): Boolean
}