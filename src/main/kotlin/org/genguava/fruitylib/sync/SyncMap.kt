package org.genguava.fruitylib.sync

import com.google.common.collect.Maps
import com.google.common.collect.Sets
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.ByteBufUtils
import org.genguava.fruitylib.util.ByteUtils
import org.genguava.fruitylib.util.Log
import org.genguava.fruitylib.util.events.Event
import java.io.*
import java.util.*
import kotlin.experimental.and


abstract class SyncMap<out H : ISyncMapProvider>(protected val handler: H) {
    companion object {
        class SyncFieldException : RuntimeException {
            constructor(cause: Throwable, name: String) : super("Failed to sync field '$name'", cause)
            constructor(cause: Throwable, index: Int) : super("Failed to sync field $index", cause)
        }

        private val MAX_OBJECT_NUM = 16

        @Throws(IOException::class)
        fun findSyncMap(world: World, input: DataInput): ISyncMapProvider? {
            val handlerTypeId = ByteUtils.readVLI(input)

            require(handlerTypeId < HandlerType.TYPES.size) { "SERIOUS BUG!!! handler type" }

            val handlerType = HandlerType.TYPES[handlerTypeId]
            val handler = handlerType.findHandler(world, input)
            return handler
        }
    }

    class SyncEvent(val changes: Set<ISyncableObject>)

    abstract class HandlerType {
        companion object {
            val ENTITY = object: HandlerType() {
                override val ordinal = 1

                override fun findHandler(world: World, input: DataInput): ISyncMapProvider? {
                    val entityId = input.readInt()
                    val entity = world.getEntityByID(entityId)
                    if(entity is ISyncMapProvider) return entity

                    Log.warn("Invalid handler info: can't find ISyncHandler multiblockEntity id $entityId")
                    return null
                }

                override fun writeHandlerInfo(handler: ISyncMapProvider, output: DataOutput) {
                    try {
                        val e = handler as Entity
                        output.writeInt(e.entityId)
                    } catch (e: ClassCastException) {
                        throw RuntimeException("Invalid usage of handler type", e)
                    }
                }
            }

            val TILE_ENTITY = object: HandlerType() {
                override val ordinal = 0

                override fun findHandler(world: World, input: DataInput): ISyncMapProvider? {
                    val x = input.readInt()
                    val y = input.readInt()
                    val z = input.readInt()
                    val blockPos = BlockPos(x, y ,z)

                    if(!world.isAirBlock(blockPos)) {
                        val tile = world.getTileEntity(blockPos)
                        if(tile is ISyncMapProvider) return tile
                    }

                    Log.warn("Invalid handler info: can't find ISyncHandler TE @ ($x,$y,$z)", x, y, z)
                    return null
                }

                override fun writeHandlerInfo(handler: ISyncMapProvider, output: DataOutput) {
                    try {
                        val tileEntity = handler as TileEntity
                        output.writeInt(tileEntity.pos.x)
                        output.writeInt(tileEntity.pos.y)
                        output.writeInt(tileEntity.pos.z)
                    } catch (e: ClassCastException) {
                        throw RuntimeException("Invalid usage of handler type", e)
                    }
                }
            }

            internal val TYPES = arrayOf(TILE_ENTITY, ENTITY)
        }

        @Throws(IOException::class)
        abstract fun findHandler(world: World, input: DataInput): ISyncMapProvider?

        @Throws(IOException::class)
        abstract fun writeHandlerInfo(handler: ISyncMapProvider, output: DataOutput)
        abstract val ordinal: Int
    }

    private val knownUsers = hashSetOf<Int>()

    private val objects = arrayOfNulls<ISyncableObject>(16)
    private val nameMap = hashMapOf<String, ISyncableObject>()
    private val objectToId = Maps.newIdentityHashMap<ISyncableObject, Int>()

    val outboundSyncEvent = Event<SyncEvent>()
    val inboundSyncEvent = Event<SyncEvent>()
    val clientInitEvent = Event<SyncEvent>()

    private var index = 0

    val size get() = index

    fun put(name: String, value: ISyncableObject) {
        require(index < MAX_OBJECT_NUM) { "Can't add more than $MAX_OBJECT_NUM objects" }
        val objId = index++
        objects[objId] = value
        nameMap[name] = value
        val prev = objectToId.put(value, objId)
        require(prev == null) { "Object $value registered twice, under ids $prev and $objId" }
    }

    operator fun set(name: String, value: ISyncableObject) = put(name, value)

    operator fun get(name: String) = nameMap["name"] ?: throw NoSuchElementException(name)
    operator fun get(objectId: Int): ISyncableObject {
        try {
            return objects[objectId] ?: throw NoSuchElementException(objectId.toString())
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw NoSuchElementException(objectId.toString())
        }
    }

    fun getId(objectt: ISyncableObject): Int {
        return objectToId[objectt] ?: throw NoSuchElementException(objectt.toString())
    }

    @Throws(IOException::class)
    fun readFromStream(dataInputStream: DataInputStream) {
        var mask = dataInputStream.readShort()
        val changes = Sets.newIdentityHashSet<ISyncableObject>()
        var currentBit = 0

        while(mask != 0.toShort()) {
            if((mask and 1.toShort()) != 0.toShort()) {
                val objectt = objects[currentBit]
                if(objectt != null) {
                    try {
                        objectt.readFromStream(dataInputStream)
                    } catch (e: Throwable) {
                        throw SyncFieldException(e, currentBit)
                    }
                    changes.add(objectt)
                }
            }
            currentBit++
            mask = (mask.toInt() shr 1).toShort()
        }

        if(!changes.isEmpty()) inboundSyncEvent.fire(SyncEvent(Collections.unmodifiableSet(changes)))
    }

    @Throws(IOException::class) fun writeToStream(dataOutputStream: DataOutputStream, fullPacket: Boolean) {
        var mask = 0
        for(i in 0..index-1) {
            val objectt = objects[i]
            if(objectt != null && (fullPacket || objectt.isDirty())) {
                mask = ByteUtils.on(mask, i)
            }
        }
        dataOutputStream.writeShort(mask)

        for(i in 0..index-1) {
            val objectt = objects[i]
            if(objectt != null && (fullPacket || objectt.isDirty())) {
                try {
                    objectt.writeToStream(dataOutputStream)
                } catch (t: Throwable) {
                    throw SyncFieldException(t, i)
                }
            }
        }
    }

    protected abstract val handlerType: HandlerType
    protected abstract val playersWatching: Set<EntityPlayerMP>
    protected abstract val world: World
    protected abstract val invalid: Boolean

    fun sync() {
        require(!world.isRemote) { "This method can only be used server side" }
        if(invalid) return

        val changes = listChanges()
        val hasChanges = !changes.isEmpty()

        val fullPacketTargets = arrayListOf<EntityPlayerMP>()
        val deltaPacketTargets = arrayListOf<EntityPlayerMP>()

        val players = playersWatching
        for(player in players) {
            if(knownUsers.contains(player.entityId)) {
                if(hasChanges) deltaPacketTargets.add(player)
            } else {
                knownUsers.add(player.entityId)
                fullPacketTargets.add(player)
            }
        }

        try {
            if(!deltaPacketTargets.isEmpty()) {
                val deltaPayload = createPayload(false)
                SyncChannelHolder.instance.value.sendPayloadToPlayers(deltaPayload, deltaPacketTargets)
            }
        } catch (e: IOException) {
            Log.warn(e, "IOError during delta sync")
        }

        try {
            if(!fullPacketTargets.isEmpty()) {
                val fullPayload = createPayload(true)
                SyncChannelHolder.instance.value.sendPayloadToPlayers(fullPayload, fullPacketTargets)
            }
        } catch (e: IOException) {
            Log.warn(e, "IOError during full sync")
        }

        if(hasChanges) markClean(changes)
        outboundSyncEvent.fire(SyncEvent(Collections.unmodifiableSet(changes)))
    }

    @Suppress("UNCHECKED_CAST")
    private fun listChanges() = objects.filter { it != null && it.isDirty() }.toSet() as Set<ISyncableObject>
    private fun markClean(changes: Set<ISyncableObject>) = changes.forEach { it.markClean() }

    @Throws(IOException::class)
    fun createPayload(fullPacket: Boolean): PacketBuffer {
        val output = Unpooled.buffer()
        val type = handlerType
        ByteBufUtils.writeVarInt(output, type.ordinal, 5)

        val dataOutputStream = DataOutputStream(ByteBufOutputStream(output))
        type.writeHandlerInfo(handler, dataOutputStream)
        writeToStream(dataOutputStream, fullPacket)

        return PacketBuffer(output.copy())
    }

    fun writeToNBT(tag: NBTTagCompound) {
        for((name, obj) in nameMap.entries) {
            try {
                obj.writeToNBT(tag, name)
            } catch (e: Throwable) {
                throw SyncFieldException(e, name)
            }
        }
    }

    fun readFromNBT(tag: NBTTagCompound) {
        for((name, obj) in nameMap.entries) {
            try {
                obj.readFromNBT(tag, name)
            } catch (e: Throwable) {
                throw SyncFieldException(e, name)
            }
        }
    }
}
