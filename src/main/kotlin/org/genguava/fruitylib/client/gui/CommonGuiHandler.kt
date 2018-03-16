package org.genguava.fruitylib.client.gui

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import org.genguava.fruitylib.blocks.FruityBlock


open class CommonGuiHandler(protected val wrappedHandler: IGuiHandler? = null) : IGuiHandler {

    override fun getServerGuiElement(ID: Int, player: EntityPlayer, world: World?, x: Int, y: Int, z: Int): Any? {
        if(ID != FruityBlock.FRUITY_LIB_TE_GUI) return wrappedHandler?.getServerGuiElement(ID, player, world, x, y, z)
        else {
            val tile = world?.getTileEntity(BlockPos(x, y, z)) ?: return null
            if(tile is IHasGui) return tile.getServerGui(player)
        }
        return null
    }

    override fun getClientGuiElement(ID: Int, player: EntityPlayer, world: World?, x: Int, y: Int, z: Int): Any? {
        return null
    }
}