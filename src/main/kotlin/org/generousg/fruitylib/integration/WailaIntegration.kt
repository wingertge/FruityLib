package org.generousg.fruitylib.integration

import mcp.mobius.waila.api.IWailaConfigHandler
import mcp.mobius.waila.api.IWailaDataAccessor
import mcp.mobius.waila.api.IWailaDataProvider
import mcp.mobius.waila.api.IWailaRegistrar
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.generousg.fruitylib.FruityLib
import org.generousg.fruitylib.blocks.FruityBlock

class WailaIntegration private constructor(): IWailaDataProvider {
    companion object {
        private var _instance: WailaIntegration? = null
        val instance: WailaIntegration get() {
            if(_instance == null) _instance = WailaIntegration()
            return _instance!!
        }

        fun callbackRegister(registrar: IWailaRegistrar) {
            registrar.registerHeadProvider(instance, FruityBlock::class.java)
        }
    }

    override fun getWailaTail(itemStack: ItemStack?, currenttip: MutableList<String>?, accessor: IWailaDataAccessor?, config: IWailaConfigHandler?): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNBTData(player: EntityPlayerMP?, te: TileEntity?, tag: NBTTagCompound?, world: World?, pos: BlockPos?): NBTTagCompound {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Suppress("DEPRECATION")
    override fun getWailaHead(itemStack: ItemStack, currenttip: MutableList<String>, accessor: IWailaDataAccessor, config: IWailaConfigHandler): MutableList<String> {
        val block = accessor.block
        if(block is FruityBlock && block.hasInfo) block.addInformation(accessor.stack, accessor.player, currenttip, false)
        if(FruityLib.DEBUG_MODE) {
            for ((key, value) in accessor.block.getActualState(accessor.blockState, accessor.world, accessor.position).properties) {
                currenttip.add("${key.getName()}: ${(value as Enum<*>).name}")
            }
        }
        return currenttip
    }

    override fun getWailaBody(itemStack: ItemStack?, currenttip: MutableList<String>?, accessor: IWailaDataAccessor?, config: IWailaConfigHandler?): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWailaStack(accessor: IWailaDataAccessor, config: IWailaConfigHandler): ItemStack {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}