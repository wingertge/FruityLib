package org.generousg.fruitylib.sync

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.Packet
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.items.IItemHandler
import org.generousg.fruitylib.network.DimCoord
import org.generousg.fruitylib.network.rpc.IRpcTarget
import org.generousg.fruitylib.network.rpc.IRpcTargetProvider
import org.generousg.fruitylib.network.rpc.RpcCallDispatcher
import org.generousg.fruitylib.network.rpc.targets.EntityRpcTarget
import org.generousg.fruitylib.network.senders.IPacketSender
import org.generousg.fruitylib.reflect.TypeUtils
import java.io.DataInputStream
import java.io.DataOutputStream


abstract class SyncedEntity(world: World) : Entity(world), ISyncMapProvider, IEntityAdditionalSpawnData, IRpcTargetProvider {
    companion object {
        @CapabilityInject(IFluidHandler::class)
        lateinit var FLUID_HANDLER_CAPABILITY: Capability<IFluidHandler>
        @CapabilityInject(IItemHandler::class)
        lateinit var ITEM_HANDLER_CAPABILITY: Capability<IItemHandler>
    }

    override val syncMap: SyncMapEntity<SyncedEntity> = SyncMapEntity(this)
    val dimCoords get() = DimCoord(world.provider.dimension, posX.toInt(), posY.toInt(), posZ.toInt())

    init {
        createSyncedFields()
        SyncObjectScanner.instance.registerAllFields(syncMap, this)
    }

    override fun writeEntityToNBT(compound: NBTTagCompound) {
        syncMap.writeToNBT(compound)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound) {
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
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    @Suppress("UNUSED_PARAMETER")
    fun onTracked(event: PlayerEvent.StartTracking) {
        //if(!world.isRemote) sync()
    }

    override fun getEntityData(): NBTTagCompound {
        val tag = NBTTagCompound()
        syncMap.writeToNBT(NBTTagCompound())
        return tag
    }

    override fun onEntityUpdate() {
        super.onEntityUpdate()
        if(isDead) {
            cleanup()
            world.removeEntity(this)
        }
    }

    override fun createRpcTarget(): IRpcTarget = EntityRpcTarget(this)
    fun <T> createProxy(sender: IPacketSender, mainIntf: Class<out T>, vararg extraIntf: Class<out T>): T {
        TypeUtils.requireIsInstance(this, mainIntf, *extraIntf)
        return RpcCallDispatcher.instance.createProxy(createRpcTarget(), sender, mainIntf, *extraIntf)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createClientRpcProxy(mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        val sender = RpcCallDispatcher.instance.senders.client
        return createProxy(sender, mainIntf, *(extraIntf as Array<out Class<out T>>))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> createServerRpcProxy(mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        val sender = RpcCallDispatcher.instance.senders.entity.bind(this)
        return createProxy(sender, mainIntf, *(extraIntf as Array<out Class<out T>>))
    }

    open fun cleanup() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }
}
