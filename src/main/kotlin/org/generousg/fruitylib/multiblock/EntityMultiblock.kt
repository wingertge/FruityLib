package org.generousg.fruitylib.multiblock

import net.minecraft.block.Block
import net.minecraft.util.DamageSource
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.generousg.fruitylib.sync.SyncedEntity


abstract class EntityMultiblock(world: World) : SyncedEntity(world) {
    abstract fun destroy()
    override fun canRenderOnFire(): Boolean = false
    override fun canBeCollidedWith(): Boolean = false
    override fun canBePushed(): Boolean = false
    override fun dealFireDamage(amount: Int) = Unit
    override fun attackEntityFrom(source: DamageSource?, amount: Float): Boolean = false
    override fun canTrample(world: World?, block: Block?, pos: BlockPos?, fallDistance: Float): Boolean = false
}