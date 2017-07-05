package org.generousg.fruitylib.sync

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import java.io.DataInputStream
import java.io.DataOutputStream


abstract class SyncedEntity(world: World) : Entity(world), ISyncMapProvider, IEntityAdditionalSpawnData {

    override val syncMap: SyncMapEntity<SyncedEntity> = SyncMapEntity(this)

    init {
        createSyncedFields()
        SyncObjectScanner.instance.registerAllFields(syncMap, this)
    }

    override fun writeEntityToNBT(compound: NBTTagCompound) {
        super.writeToNBT(compound)
        syncMap.writeToNBT(compound)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound) {
        super.readFromNBT(compound)
        syncMap.readFromNBT(compound)
    }

    protected abstract fun createSyncedFields()
    fun addSyncedObject(name: String, obj: ISyncableObject) { syncMap.put(name, obj) }
    fun sync() = syncMap.sync()

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

    override fun entityInit() {

    }

    override fun getEntityData(): NBTTagCompound {
        val tag = NBTTagCompound()
        syncMap.writeToNBT(NBTTagCompound())
        return tag
    }
}