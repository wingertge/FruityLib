package org.generousg.fruitylib.tileentity

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import org.generousg.fruitylib.blocks.FruityBlock
import org.generousg.fruitylib.inventory.InventorySerializable
import org.generousg.fruitylib.network.DimCoord
import org.generousg.fruitylib.network.rpc.IRpcTarget
import org.generousg.fruitylib.network.rpc.IRpcTargetProvider
import org.generousg.fruitylib.network.rpc.RpcCallDispatcher
import org.generousg.fruitylib.network.rpc.targets.TileEntityRpcTarget
import org.generousg.fruitylib.network.senders.IPacketSender
import org.generousg.fruitylib.reflect.TypeUtils


abstract class FruityTileEntity : TileEntity(), IRpcTargetProvider {
    private val isUsedForClientInventoryRendering = false

    val dimCoords get() = DimCoord(world.provider.dimension, getPos().x, getPos().y, getPos().z)
    val block by lazy { world.getBlockState(pos).block }

    /** Place for TE specific setup. Called once upon creation */
    open fun setup() {}
    fun isAddedToWorld() = world != null

    private fun getTileEntity(pos: BlockPos) = if(world != null && !world.isAirBlock(pos)) world.getTileEntity(pos) else null
    fun getTileInDirection(direction: EnumFacing) = getNeighbor(direction.frontOffsetX, direction.frontOffsetY, direction.frontOffsetZ)
    fun getNeighbor(dx: Int, dy: Int, dz: Int) = getTileEntity(BlockPos(pos.x + dx, pos.y + dy, pos.z + dz))

    override fun toString(): String = "(${pos.x},${pos.y},${pos.z})"

    fun isAirBlock(facing: EnumFacing) = world?.isAirBlock(BlockPos(pos.x + facing.frontOffsetX, pos.y + facing.frontOffsetY, pos.z + facing.frontOffsetZ)) ?: false
    fun sendBlockEvent(event: Int, param: Int) = world?.addBlockEvent(pos, blockType, event, param)
    fun openGui(instance: Any, player: EntityPlayer) = player.openGui(instance, FruityBlock.FRUITY_LIB_TE_GUI, world, pos.x, pos.y, pos.z)
    fun isRenderedInInventory() = isUsedForClientInventoryRendering
    override fun createRpcTarget(): IRpcTarget = TileEntityRpcTarget(this)
    fun <T> createProxy(sender: IPacketSender, mainIntf: Class<out T>, vararg extraIntf: Class<out T>): T {
        TypeUtils.isInstance(this, mainIntf, *extraIntf)
        return RpcCallDispatcher.instance.value.createProxy(createRpcTarget(), sender, mainIntf, *extraIntf)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createClientRpcProxy(mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        val sender = RpcCallDispatcher.instance.value.senders.client
        return createProxy(sender, mainIntf, *(extraIntf as Array<out Class<out T>>))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createServerRpcProxy(mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        val sender = RpcCallDispatcher.instance.value.senders.block.bind(dimCoords)
        return createProxy(sender, mainIntf, *(extraIntf as Array<out Class<out T>>))
    }

    fun markUpdated() = world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2)

    protected fun registerInventoryCallback(inventory: InventorySerializable): InventorySerializable {
        inventory.inventoryChangedEvent += { markUpdated() }
        return inventory
    }

    fun isValid(player: EntityPlayer): Boolean {
        return world.getTileEntity(pos) === this && player.getDistanceSq(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5) <= 64.0
    }
}