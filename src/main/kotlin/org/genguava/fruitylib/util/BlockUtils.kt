package org.genguava.fruitylib.util

import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


object BlockUtils {

    fun getTileInDirection(world: World, coord: BlockPos, direction: EnumFacing): TileEntity? {
        val targetX = coord.x + direction.frontOffsetX
        val targetY = coord.y + direction.frontOffsetY
        val targetZ = coord.z + direction.frontOffsetY
        return world.getTileEntity(BlockPos(targetX, targetY, targetZ))
    }

    fun getTileInDirectionSafe(world: World, coord: BlockPos, direction: EnumFacing): TileEntity? {
        val targetX = coord.x + direction.frontOffsetX
        val targetY = coord.y + direction.frontOffsetY
        val targetZ = coord.z + direction.frontOffsetZ
        val newPos = BlockPos(targetX, targetY, targetZ)
        return if (!world.isAirBlock(newPos)) world.getTileEntity(newPos) else null
    }
}