package org.genguava.fruitylib.liquids

import com.google.common.base.Function
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.FluidTank
import net.minecraftforge.fluids.capability.FluidTankProperties
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fluids.capability.IFluidTankProperties
import org.genguava.fruitylib.util.BlockUtils
import org.genguava.fruitylib.util.CollectionUtils
import java.util.*


open class GenericTank : FluidTank, IExtendedFluidHandler {
    private var surroundingTanks: List<EnumFacing> = Lists.newArrayList()
    private val canAccept: (FluidStack) -> Boolean

    constructor(capacity: Int) : super(capacity) {
        this.canAccept = NO_RESTRICTIONS
    }

    constructor(capacity: Int, vararg acceptableFluids: FluidStack) : super(capacity) {
        this.canAccept = filter(*acceptableFluids)
    }

    constructor(capacity: Int, vararg acceptableFluids: Fluid) : super(capacity) {
        this.canAccept = Companion.filter(*CollectionUtils.transform(acceptableFluids.asList(), FLUID_CONVERTER))
    }

    override fun drain(resource: FluidStack?, doDrain: Boolean): FluidStack? {
        if (resource == null ||
                fluid == null ||
                fluid!!.isFluidEqual(resource))
            return null

        return drain(resource.amount, doDrain)
    }

    val space: Int
        get() = getCapacity() - fluidAmount

    override fun fill(resource: FluidStack?, doFill: Boolean): Int {
        if (resource == null || !canAccept(resource)) return 0
        return super.fill(resource, doFill)
    }

    fun updateNeighbours(world: World, coord: BlockPos, sides: Set<EnumFacing>) {
        this.surroundingTanks = Lists.newArrayList(Sets.difference(getSurroundingTanks(world, coord), sides))
    }

    fun updateNeighbours(world: World, coord: BlockPos) {
        this.surroundingTanks = Lists.newArrayList(getSurroundingTanks(world, coord))
    }

    fun distributeToSides(amountPerTick: Int, world: World?, coord: BlockPos, allowedSides: Set<EnumFacing>?) {
        if (world == null) return

        if (fluidAmount <= 0) return

        if (surroundingTanks.isEmpty()) return

        val sides = Lists.newArrayList(surroundingTanks)

        if (allowedSides != null) {
            sides.retainAll(allowedSides)
            if (sides.isEmpty()) return
        }

        val drainedFluid = drain(amountPerTick, false)

        if (drainedFluid != null && drainedFluid.amount > 0) {
            val startingAmount = drainedFluid.amount
            sides.shuffle()

            for (side in sides) {
                if (drainedFluid.amount <= 0) break

                val otherTank = BlockUtils.getTileInDirection(world, coord, side)
                if (otherTank != null) drainedFluid.amount -= tryFillNeighbour(drainedFluid, otherTank)
            }

            // return any remainder
            val distributed = startingAmount - drainedFluid.amount
            if (distributed > 0) drain(distributed, true)
        }
    }

    @JvmOverloads fun fillFromSides(maxAmount: Int, world: World?, coord: BlockPos, allowedSides: Set<EnumFacing>? = null) {
        if (world == null) return

        var toDrain = Math.min(maxAmount, space)
        if (toDrain <= 0) return

        if (surroundingTanks.isEmpty()) return

        val sides = Lists.newArrayList(surroundingTanks)

        if (allowedSides != null) {
            sides.retainAll(allowedSides)
            if (sides.isEmpty()) return
        }

        sides.shuffle()
        for (side in sides) {
            if (toDrain <= 0) break
            toDrain -= fillInternal(world, coord, side, toDrain)
        }
    }

    override fun getTankProperties(): Array<IFluidTankProperties> {
        return arrayOf(FluidTankProperties(fluid, capacity, canFill, canDrain))
    }

    fun fillFromSide(world: World, coord: BlockPos, side: EnumFacing): Int {
        val maxDrain = space
        if (maxDrain <= 0) return 0

        return fillInternal(world, coord, side, maxDrain)
    }

    fun fillFromSide(maxDrain: Int, world: World, coord: BlockPos, side: EnumFacing): Int {
        var maxDrain1 = maxDrain
        maxDrain1 = Math.max(maxDrain1, space)
        if (maxDrain1 <= 0) return 0

        return fillInternal(world, coord, side, maxDrain1)
    }

    private fun fillInternal(world: World, coord: BlockPos, side: EnumFacing, maxDrain0: Int): Int {
        var maxDrain = maxDrain0
        var drain = 0
        val otherTank = BlockUtils.getTileInDirection(world, coord, side)

        if (otherTank is IFluidHandler) {
            val handler = otherTank as IFluidHandler
            val infos = handler.tankProperties ?: return 0

            for (info in infos) {
                if (canAccept(info.contents!!)) {
                    val drained = handler.drain(maxDrain, true)

                    if (drained != null) {
                        fill(drained, true)
                        drain += drained.amount
                        maxDrain -= drained.amount
                        if (maxDrain <= 0) break
                    }
                }
            }
        }

        return drain
    }

    override fun isEmpty(): Boolean = fluid == null || fluid!!.fluid == null || fluid!!.amount == 0
    override fun isFull(): Boolean = fluid != null && fluid!!.fluid != null && fluid!!.amount >= capacity

    companion object {

        private val NO_RESTRICTIONS: (FluidStack) -> Boolean = { true }
        private val FLUID_CONVERTER = Function<Fluid, FluidStack> { input -> FluidStack(input, 0) }

        private fun filter(vararg acceptableFluids: FluidStack): (FluidStack) -> Boolean {
            if (acceptableFluids.isEmpty()) return NO_RESTRICTIONS

            return { stack -> acceptableFluids.any { it.isFluidEqual(stack) }}
        }

        private fun isNeighbourTank(world: World, coord: BlockPos, dir: EnumFacing): Boolean {
            val tile = BlockUtils.getTileInDirectionSafe(world, coord, dir)
            return tile is IFluidHandler
        }

        private fun getSurroundingTanks(world: World, coord: BlockPos): Set<EnumFacing> {
            val result = EnumSet.noneOf(EnumFacing::class.java)

            EnumFacing.VALUES.filterTo(result) { isNeighbourTank(world, coord, it) }

            return result
        }

        private fun tryFillNeighbour(drainedFluid: FluidStack, otherTank: TileEntity): Int {
            val toFill = drainedFluid.copy()

            if (otherTank is IFluidHandler) return otherTank.fill(toFill, true)
            return 0
        }
    }

}
