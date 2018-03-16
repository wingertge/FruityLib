package org.genguava.fruitylib.util

import net.minecraft.world.World
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.relauncher.Side
import org.genguava.fruitylib.FruityLib


class WorldUtils {
    companion object {
        fun getWorld(dimensionId: Int): World? {
            val result = if(FMLCommonHandler.instance().effectiveSide == Side.SERVER) FruityLib.proxy.getServerWorld(dimensionId) else FruityLib.proxy.clientWorld
            require(result?.provider?.dimension == dimensionId) { "Invalid client dimension id $dimensionId" }
            return result
        }
    }
}
