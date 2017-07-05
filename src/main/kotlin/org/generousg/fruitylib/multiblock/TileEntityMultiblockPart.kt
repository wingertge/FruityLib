package org.generousg.fruitylib.multiblock

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.generousg.fruitylib.blocks.FruityBlock
import org.generousg.fruitylib.client.gui.IHasGui
import org.generousg.fruitylib.sync.SyncableInt
import org.generousg.fruitylib.tileentity.SyncedTileEntity
import kotlin.reflect.KClass


abstract class TileEntityMultiblockPart(vararg private val validComponents: KClass<out FruityBlock>) : SyncedTileEntity(), IHasGui, ITickable {

    init {
        syncMap.receivedSyncEvent += {
            val state = block.getActualState(block.blockState.baseState, world, pos)
            world.setBlockState(pos, state, 0)
        }
    }

    override fun createSyncedFields() {
        multiblockId = SyncableInt()
    }

    override fun getServerGui(player: EntityPlayer): Any? {
        if(multiblockId.value == 0) return null
        val entity = world.getEntityByID(multiblockId.value)
        return if(entity is IHasGui) entity.getServerGui(player) else null
    }

    override fun getClientGui(player: EntityPlayer): Any? {
        if(multiblockId.value == 0) return null
        val entity = world.getEntityByID(multiblockId.value)
        return if(entity is IHasGui) entity.getClientGui(player) else null
    }

    override fun canOpenGui(player: EntityPlayer): Boolean {
        if(multiblockId.value == 0) return false
        val entity = world.getEntityByID(multiblockId.value)
        return if(entity is IHasGui) entity.canOpenGui(player) else false
    }

    fun startRebuild(neighborPos: BlockPos) {
        val te = world.getTileEntity(neighborPos)
        if(te is TileEntityMultiblockPart && te.multiblockId.value != 0) return
        val entity = rebuild(pos)
        if(entity != null)
            world.spawnEntity(entity)
    }

    abstract fun rebuild(pos: BlockPos): EntityMultiblock?

    override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newSate: IBlockState): Boolean {
        return oldState.block != newSate.block
    }

    lateinit var multiblockId: SyncableInt

    override fun update() {
        if (!world.isRemote && multiblockId.isDirty()) sync()
    }
}