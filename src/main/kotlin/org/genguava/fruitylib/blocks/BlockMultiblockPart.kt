package org.genguava.fruitylib.blocks

import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import org.genguava.fruitylib.multiblock.MultiblockPart
import org.genguava.fruitylib.multiblock.TileEntityMultiblockPart
import kotlin.reflect.KClass


@Suppress("OverridingDeprecatedMember")
abstract class BlockMultiblockPart(material: Material) : FruityBlock(material) {
    companion object {
        val MB_PART = PropertyEnum.create("mb_part", MultiblockPart::class.java)
    }

    init {
        placeEvent += {
            if(!it.world.isRemote) {
                val te = it.world.getTileEntity(it.pos) as TileEntityMultiblockPart
                te.startRebuild(it.pos)
            }
        }
        /*neighborChangedEvent += {
            if(FMLCommonHandler.instance().effectiveSide == Side.SERVER) {
                val te = it.world.getTileEntity(it.pos) as TileEntityMultiblockPart
                te.startRebuild(it.pos)
            }
        }*/
        breakEvent += {
            if(!it.world.isRemote) {
                val te = it.world.getTileEntity(it.pos) as TileEntityMultiblockPart
                te.multiblockEntity?.destroy(it.pos)
            }
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun getStateFromMeta(meta: Int): IBlockState {
        val facing = EnumFacing.getFront(meta + 2)
        return defaultState.withProperty(FACING, facing)
    }

    override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, FACING, MB_PART)

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState {
        return getMultiblockPart(worldIn, pos, state)
    }

    abstract fun getMultiblockPart(world: IBlockAccess, pos: BlockPos, state: IBlockState): IBlockState

    abstract fun getValidComponents(): List<KClass<out FruityBlock>>
}
