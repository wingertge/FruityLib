package org.generousg.fruitylib.sync

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import java.io.DataInputStream
import java.io.DataOutputStream


abstract class SyncedEntity(world: World) : Entity(world), ISyncMapProvider, IEntityAdditionalSpawnData {
    override val syncMap: SyncMapEntity<SyncedEntity> = SyncMapEntity(this)

    init {
        createSyncedFields()
        SyncObjectScanner.instance.registerAllFields(syncMap, this)

        syncMap.syncEvent += { markDirty() }
    }

    override fun writeEntityToNBT(compound: NBTTagCompound) {
        syncMap.writeToNBT(compound)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound) {
        syncMap.readFromNBT(compound)
    }

    override fun entityInit() {

    }

    protected abstract fun createSyncedFields()
    fun addSyncedObject(name: String, obj: ISyncableObject) { syncMap.put(name, obj) }
    fun sync() = syncMap.sync()

    fun markDirty() {
        (world as WorldServer).entityTracker.sendToTracking(this, createPacket(false))
    }

    override fun readSpawnData(additionalData: ByteBuf) {
        syncMap.readFromStream(DataInputStream(ByteBufInputStream(additionalData)))
    }

    override fun writeSpawnData(buffer: ByteBuf) {
        syncMap.writeToStream(DataOutputStream(ByteBufOutputStream(buffer)), true)
    }

    protected fun createPacket(fullPacket: Boolean): Packet<*> {
        val payload = syncMap.createPayload(fullPacket)
        return SyncChannelHolder.createPacket(payload)
    }
}