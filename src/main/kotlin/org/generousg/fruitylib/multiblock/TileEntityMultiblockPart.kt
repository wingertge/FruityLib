package org.generousg.fruitylib.multiblock

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.generousg.fruitylib.client.gui.IHasGui
import org.generousg.fruitylib.sync.SyncableUUID
import org.generousg.fruitylib.tileentity.SyncedTileEntity


abstract class TileEntityMultiblockPart : SyncedTileEntity(), IHasGui, ITickable {
    private var rebuiltThisTick = false
    private var entityId = 0
    val multiblockEntity: EntityMultiblock? get() {
        if(multiblockId.value == SyncableUUID.IDENTITY) return null
        if(entityId != 0) {
            val entity = world.getEntityByID(entityId)
            if(entity != null) return entity as EntityMultiblock
        }
        val entity = world.getLoadedEntityList().firstOrNull { it.persistentID == multiblockId.value }
        entityId = entity?.entityId ?: 0
        return entity as? EntityMultiblock
    }

    init {
        syncMap.inboundSyncEvent += {
            if(it.changes.contains(multiblockId)) {
                @Suppress("DEPRECATION")
                val state = block.getActualState(block.blockState.baseState, world, pos)
                world.setBlockState(pos, state, 0)
            }
        }
    }

    override fun createSyncedFields() {
        multiblockId = SyncableUUID()
    }

    override fun getServerGui(player: EntityPlayer): Any? {
        if(multiblockId.value == SyncableUUID.IDENTITY) return null
        val entity = this.multiblockEntity //cache multiblockEntity for smart cast
        return if(entity is IHasGui) entity.getServerGui(player) else null
    }

    override fun getClientGui(player: EntityPlayer): Any? {
        if(multiblockId.value == SyncableUUID.IDENTITY) return null
        val entity = this.multiblockEntity
        return if(entity is IHasGui) entity.getClientGui(player) else null
    }

    override fun canOpenGui(player: EntityPlayer): Boolean {
        if(multiblockId.value == SyncableUUID.IDENTITY) return false
        val entity = this.multiblockEntity
        return if(entity is IHasGui) entity.canOpenGui(player) else false
    }

    fun startRebuild(neighborPos: BlockPos) {
        if(rebuiltThisTick) return
        rebuiltThisTick = true
        val te = world.getTileEntity(neighborPos)
        if(te is TileEntityMultiblockPart && te.multiblockId.value != SyncableUUID.IDENTITY) return
        val entity = rebuild(pos)
        if(entity != null)
            world.spawnEntity(entity)
    }

    abstract fun rebuild(pos: BlockPos): EntityMultiblock?

    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState): Boolean {
        return oldState.block != newSate.block
    }

    lateinit var multiblockId: SyncableUUID

    override fun update() {
        if(rebuiltThisTick) rebuiltThisTick = false
        if (!world.isRemote && multiblockId.isDirty()) sync()
    }
}
