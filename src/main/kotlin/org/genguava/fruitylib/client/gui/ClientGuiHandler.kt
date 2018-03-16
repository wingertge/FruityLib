package org.genguava.fruitylib.client.gui

import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler
import org.genguava.fruitylib.blocks.FruityBlock


class ClientGuiHandler : CommonGuiHandler {
    constructor() : super()
    constructor(wrappedHandler: IGuiHandler) : super(wrappedHandler)

    override fun getClientGuiElement(ID: Int, player: EntityPlayer, world: World?, x: Int, y: Int, z: Int): Any? {
        if(world is WorldClient) {
            if(ID != FruityBlock.FRUITY_LIB_TE_GUI) return wrappedHandler?.getClientGuiElement(ID, player, world, x, y, z)
            else {
                val tile = world.getTileEntity(BlockPos(x, y, z))
                if(tile is IHasGui) return tile.getClientGui(player)
            }
        }
        return null
    }
}