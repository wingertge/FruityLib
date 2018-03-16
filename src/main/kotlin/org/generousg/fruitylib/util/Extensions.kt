@file:Suppress("PackageDirectoryMismatch")

package org.generousg.fruitylib

import mcjty.lib.tools.ItemStackTools
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fluids.FluidStack


@Suppress("UNCHECKED_CAST")
inline fun <reified T> createFilledArray(size: Int, initializer: (Int)->T): Array<T> {
    val tempArray = arrayOfNulls<T>(size)
    (0..tempArray.size-1).forEach { tempArray[it] = initializer.invoke(it) }
    return tempArray as Array<T>
}

fun ItemStack.isNullOrEmpty(): Boolean {
    return ItemStackTools.isEmpty(this)
}

fun ItemStack.empty() = ItemStackTools.makeEmpty(this)
val emptyItemStack get() = ItemStackTools.getEmptyStack()

fun IBlockAccess.getBlockInDirection(direction: EnumFacing, pos: BlockPos): IBlockState {
    return this.getBlockState(pos.add(Vec3i(direction.frontOffsetX, direction.frontOffsetY, direction.frontOffsetZ)))
}

fun <T> Iterable<T>.join(other: Iterable<T>): MutableCollection<T> {
    val result = arrayListOf<T>()
    this.forEach { result.add(it) }
    other.forEach { result.add(it) }
    return result
}

fun BlockPos.subtract(x: Int, y: Int, z: Int): BlockPos = this.subtract(Vec3i(x, y, z))
fun BlockPos.inDirection(direction: EnumFacing) = this.add(direction.frontOffsetX, direction.frontOffsetY, direction.frontOffsetZ)

fun FluidStack.add(otherStack: FluidStack?): FluidStack = this.add(otherStack?.amount ?: 0)

fun FluidStack.add(amount: Int): FluidStack = FluidStack(this.fluid, this.amount + amount)

fun FluidStack.subtract(otherStack: FluidStack?): FluidStack = this.subtract(otherStack?.amount ?: 0)

fun FluidStack.subtract(amount: Int): FluidStack = FluidStack(this.fluid, this.amount - amount)
