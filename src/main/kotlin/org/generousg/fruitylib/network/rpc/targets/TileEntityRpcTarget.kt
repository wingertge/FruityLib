package org.generousg.fruitylib.network.rpc.targets

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import org.generousg.fruitylib.network.rpc.IRpcTarget
import org.generousg.fruitylib.util.WorldUtils
import java.io.DataInput
import java.io.DataOutput


class TileEntityRpcTarget(private var te: TileEntity?) : IRpcTarget {
    override val target: Any?
        get() = te

    constructor() : this(null)

    override fun writeToStream(output: DataOutput) {
        if(te == null) return
        output.writeInt(te!!.world.provider.dimension)
        output.writeInt(te!!.pos.x)
        output.writeInt(te!!.pos.y)
        output.writeInt(te!!.pos.z)
    }

    override fun readFromStream(player: EntityPlayer, input: DataInput) {
        val worldId = input.readInt()
        val x = input.readInt()
        val y = input.readInt()
        val z = input.readInt()

        val world = WorldUtils.getWorld(worldId)
        te = world?.getTileEntity(BlockPos(x, y, z))
    }

    override fun afterCall() = Unit
}