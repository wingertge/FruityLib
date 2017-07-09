package org.generousg.fruitylib.tileentity

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket
import org.generousg.fruitylib.network.rpc.RpcCallDispatcher
import org.generousg.fruitylib.network.rpc.targets.SyncRpcTarget
import org.generousg.fruitylib.reflect.TypeUtils
import org.generousg.fruitylib.sync.*
import org.generousg.fruitylib.sync.drops.DropTagSerializer
import org.generousg.fruitylib.util.Log
import java.io.IOException


abstract class SyncedTileEntity : FruityTileEntity(), ISyncMapProvider {

    override val syncMap: SyncMap<*> = SyncMapTile(this)

    private var tagSerializer: DropTagSerializer? = null
    protected val chunk: Chunk by lazy { world.getChunkFromBlockCoords(pos) }

    init {
        createSyncedFields()
        SyncObjectScanner.instance.registerAllFields(syncMap, this)

        syncMap.outboundSyncEvent += { markUpdated() }
    }

    protected val dropSerializer: DropTagSerializer
        get() {
            if (tagSerializer == null) tagSerializer = DropTagSerializer()
            return tagSerializer!!
        }

    protected fun createRenderUpdateListener(): (SyncMap.SyncEvent)->Unit = { world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2) }
    protected fun createRenderUpdateListener(target: ISyncableObject): (SyncMap.SyncEvent)->Unit = { if(it.changes.contains(target)) world.markAndNotifyBlock(pos, world.getChunkFromBlockCoords(pos), world.getBlockState(pos), world.getBlockState(pos), 2) }

    protected abstract fun createSyncedFields()
    fun addSyncedObject(name: String, obj: ISyncableObject) = syncMap.put(name, obj)
    fun sync() = syncMap.sync()

    fun getDescriptionPacket(): FMLProxyPacket? {
        try {
            val payload = syncMap.createPayload(true)
            return SyncChannelHolder.createPacket(payload)
        } catch (e: IOException) {
            Log.severe(e, "Error during description packet creation")
            return null
        }
    }

    override fun writeToNBT(tag: NBTTagCompound): NBTTagCompound {
        super.writeToNBT(tag)
        syncMap.writeToNBT(tag)
        return tag
    }

    override fun readFromNBT(tag: NBTTagCompound) {
        super.readFromNBT(tag)
        syncMap.readFromNBT(tag)
    }

    fun <T> createRpcProxy(`object`: ISyncableObject, mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        TypeUtils.requireIsInstance(`object`, mainIntf, *extraIntf)
        val target = SyncRpcTarget.SyncTileEntityRpcTarget(this, `object`)
        val sender = RpcCallDispatcher.instance.senders.client
        return RpcCallDispatcher.instance.createProxy(target, sender, mainIntf, *extraIntf)
    }

    override fun getUpdateTag(): NBTTagCompound {
        return writeToNBT(NBTTagCompound())
    }
}
