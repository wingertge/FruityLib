@file:Suppress("DEPRECATION", "OverridingDeprecatedMember")

package org.generousg.fruitylib.blocks

import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.Explosion
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import org.generousg.fruitylib.client.gui.IHasGui
import org.generousg.fruitylib.inventory.IInventoryProvider
import org.generousg.fruitylib.tileentity.FruityTileEntity
import org.generousg.fruitylib.tileentity.ICustomHarvestDrops
import org.generousg.fruitylib.tileentity.ICustomPickItem
import org.generousg.fruitylib.util.events.*
import kotlin.reflect.KClass


abstract class FruityBlock(material: Material, var hasInfo: Boolean = false) : Block(material) {
    companion object {
        val FACING: PropertyDirection = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL)
        val FRUITY_LIB_TE_GUI = -2
    }

    private enum class TileEntityCapability constructor(val intf: Class<*>) {
        GUI_PROVIDER(IHasGui::class.java),
        INVENTORY(IInventory::class.java),
        INVENTORY_PROVIDER(IInventoryProvider::class.java)
    }

    internal var teClass: Class<out TileEntity>? = null
    val activateEvent = Event<BlockActivatedEvent>()
    val breakEvent = Event<BlockBreakEvent>()
    val destroyEvent = Event<BlockBrokenEvent>()
    val placeEvent = Event<BlockPlacedEvent>()
    val addEvent = Event<BlockAddedEvent>()
    val neighborChangedEvent = Event<NeighborChangedEvent>()
    protected abstract val mod: Any



    fun hasCapability(intf: KClass<*>): Boolean {
        return if(teClass != null) intf.java.isAssignableFrom(teClass) else false
    }

    override fun getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack {
        if(hasCapability(ICustomPickItem::class)) {
            val tileEntity = world.getTileEntity(pos)
            if (tileEntity is ICustomPickItem) return tileEntity.getPickBlock(player)
        }
        return super.getPickBlock(state, target, world, pos, player)
    }

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.horizontalFacing.opposite), 2)
        placeEvent.fire(BlockPlacedEvent(worldIn, pos, state, placer, stack))
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    }

    override fun onBlockDestroyedByPlayer(worldIn: World, pos: BlockPos, state: IBlockState) {
        destroyEvent.fire(BlockBrokenEvent(worldIn, pos))
        super.onBlockDestroyedByPlayer(worldIn, pos, state)
    }

    override fun onBlockDestroyedByExplosion(worldIn: World, pos: BlockPos, explosionIn: Explosion) {
        destroyEvent.fire(BlockBrokenEvent(worldIn, pos))
        super.onBlockDestroyedByExplosion(worldIn, pos, explosionIn)
    }

    override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(FACING, EnumFacing.getFront((meta and 3) + 2))
    override fun getMetaFromState(state: IBlockState): Int = state.getValue(FACING).index - 2
    override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, FACING)

    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<String>, advanced: Boolean) {
        if(hasInfo) tooltip.add(I18n.format(unlocalizedName + ".info"))
    }

    override fun hasTileEntity(): Boolean = this is ITileEntityProvider
    open fun isGuiAccessible(player: EntityPlayer) = true

    override fun onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean {
        activateEvent.fire(BlockActivatedEvent(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ))

        if(!worldIn.isRemote && hasCapability(IHasGui::class)) {
            val te = worldIn.getTileEntity(pos)
            if(te is IHasGui && te.canOpenGui(playerIn) && !playerIn.isSneaking) {
                openGui(playerIn, worldIn, pos.x, pos.y, pos.z)
                return true
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)
    }

    override fun createTileEntity(world: World?, state: IBlockState?): TileEntity? {
        val te = createTileEntity()
        if(te != null) {
            te.blockType = this
            if(te is FruityTileEntity) {
                te.setup()
            }
        }

        return te
    }

    protected open fun createTileEntity(): TileEntity? {
        try {
            return teClass?.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Failed to create TE with class $teClass", e)
        }
    }

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
        addEvent.fire(BlockAddedEvent(worldIn, pos, state))
        super.onBlockAdded(worldIn, pos, state)
    }

    override fun onNeighborChange(world: IBlockAccess, pos: BlockPos, neighbor: BlockPos) {
        val blockState = world.getBlockState(neighbor)
        neighborChangedEvent.fire(NeighborChangedEvent(world, pos, neighbor, blockState))
        super.onNeighborChange(world, pos, neighbor)
    }

    override fun getDrops(world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int): MutableList<ItemStack> {
        if(hasCapability(ICustomHarvestDrops::class)) {
            val te = world.getTileEntity(pos)
            if(te is ICustomHarvestDrops) return te.drops(super.getDrops(world, pos, state, fortune), fortune)
        }
        return super.getDrops(world, pos, state, fortune)
    }

    fun openGui(player: EntityPlayer, world: World, x: Int, y: Int, z: Int) = player.openGui(mod, FRUITY_LIB_TE_GUI, world, x, y, z)

    override fun damageDropped(state: IBlockState): Int {
        return getMetaFromState(state)
    }

    override fun breakBlock(worldIn: World, pos: BlockPos, state: IBlockState) {
        breakEvent.fire(BlockBreakEvent(worldIn, pos, state))
        super.breakBlock(worldIn, pos, state)
    }
}
