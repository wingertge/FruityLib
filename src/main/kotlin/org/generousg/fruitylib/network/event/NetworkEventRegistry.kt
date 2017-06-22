package org.generousg.fruitylib.network.event

import com.google.common.base.Preconditions
import com.google.common.base.Throwables
import com.google.common.collect.Maps
import gnu.trove.map.hash.TIntObjectHashMap
import network.event.EventDirection
import org.generousg.fruitylib.datastore.IDataVisitor


class NetworkEventRegistry : IDataVisitor<String, Int> {

    private val idToType = TIntObjectHashMap<INetworkEventType>()

    private val clsToId = Maps.newIdentityHashMap<Class<out NetworkEvent>, Int>()

    internal fun getIdForClass(cls: Class<out NetworkEvent>): Int {
        val result = clsToId[cls]
        Preconditions.checkNotNull(result, "Class %s is not registered", cls)
        return result!!
    }

    internal fun getTypeForId(id: Int): INetworkEventType {
        val result = idToType.get(id)
        Preconditions.checkNotNull(result, "Id %s is not registered", id)
        return result
    }

    override fun begin(size: Int) {
        idToType.clear()
        clsToId.clear()
    }

    override fun entry(key: String, value: Int) {
        val candidateCls: Class<*>
        try {
            candidateCls = Class.forName(key)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException(String.format("Can't find class %s", key), e)
        }

        Preconditions.checkArgument(NetworkEvent::class.java.isAssignableFrom(candidateCls))

        val cls = candidateCls as Class<out NetworkEvent>

        val type = createPacketType(cls)
        idToType.put(value, type)
        clsToId.put(cls, value)
    }

    override fun end() {}

    companion object {

        fun createPacketType(cls: Class<out NetworkEvent>): INetworkEventType {
            val meta = cls.getAnnotation(NetworkEventMeta::class.java)
            val customType = cls.getAnnotation(NetworkEventCustomType::class.java)

            if (customType != null) {
                Preconditions.checkState(meta == null, "NetworkEventMeta and NetworkEventCustomType are mutually exclusive")
                try {
                    return customType.value.java.newInstance()
                } catch (e: Exception) {
                    throw Throwables.propagate(e)
                }

            }

            val isCompressed: Boolean
            val isChunked: Boolean
            val direction: EventDirection

            if (meta != null) {
                isChunked = meta.chunked
                isCompressed = meta.compressed
                direction = meta.direction
            } else {
                isChunked = false
                isCompressed = false
                direction = EventDirection.ANY
            }

            return object : INetworkEventType {
                override val isCompressed: Boolean
                    get() = isCompressed

                override val isChunked: Boolean
                    get() = isChunked

                override val direction: EventDirection
                    get() = direction

                override fun createPacket(): NetworkEvent {
                    try {
                        return cls.newInstance()
                    } catch (e: Exception) {
                        throw Throwables.propagate(e)
                    }

                }
            }
        }
    }
}