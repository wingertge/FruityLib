package org.genguava.fruitylib.network.rpc.targets

import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import org.genguava.fruitylib.network.rpc.IRpcTarget
import org.genguava.fruitylib.util.WorldUtils
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException


class EntityRpcTarget : IRpcTarget {

    private var entity: Entity? = null

    constructor() {}

    constructor(entity: Entity) {
        this.entity = entity
    }

    override val target: Any?
        get() = entity

    @Throws(IOException::class)
    override fun writeToStream(output: DataOutput) {
        output.writeInt(entity!!.world.provider.dimension)
        output.writeInt(entity!!.entityId)
    }

    @Throws(IOException::class)
    override fun readFromStream(player: EntityPlayer, input: DataInput) {
        val worldId = input.readInt()
        val entityId = input.readInt()

        val world = WorldUtils.getWorld(worldId)
        entity = world!!.getEntityByID(entityId)
    }

    override fun afterCall() {}
}