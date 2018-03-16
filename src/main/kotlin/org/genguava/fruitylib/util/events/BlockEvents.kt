@file:Suppress("unused")

package org.genguava.fruitylib.util.events

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World


class BlockActivatedEvent(val world: World, val pos: BlockPos, val state: IBlockState, val player: EntityPlayer, val hand: EnumHand, val facing: EnumFacing, val hitx: Float, val hitY: Float, val hitZ: Float)
class BlockBrokenEvent(val world: World, val pos: BlockPos)
class BlockBreakEvent(val world: World, val pos: BlockPos, val state: IBlockState)
class BlockPlacedEvent(val world: World, val pos: BlockPos, val state: IBlockState, val placer: EntityLivingBase, val stack: ItemStack)
class BlockAddedEvent(val world: World, val pos: BlockPos, val state: IBlockState)
class NeighborChangedEvent(val world: IBlockAccess, val pos: BlockPos, val neighborPos: BlockPos, val neighborState: IBlockState)