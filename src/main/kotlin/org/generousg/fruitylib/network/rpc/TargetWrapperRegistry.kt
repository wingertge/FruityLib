package org.generousg.fruitylib.network.rpc

import com.google.common.base.Preconditions
import com.google.common.base.Throwables
import com.google.common.collect.HashBiMap
import org.generousg.fruitylib.datastore.IDataVisitor


class TargetWrapperRegistry : IDataVisitor<String, Int> {
    private val wrapperCls = HashBiMap.create<Class<out IRpcTarget>, Int>()

    override fun begin(size: Int) {
        wrapperCls.clear()
    }

    override fun entry(key: String, value: Int) {
        val cls: Class<*>
        try {
            cls = Class.forName(key)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException(String.format("Failed to load class %s", key), e)
        }

        Preconditions.checkArgument(IRpcTarget::class.java.isAssignableFrom(cls), "Class %s is not ITargetWrapper", cls)

        try {
            cls.getConstructor()
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException(String.format("Class %s has no parameterless constructor", key), e)
        } catch (e: Exception) {
            throw Throwables.propagate(e)
        }

        @Suppress("UNCHECKED_CAST")
        val wrapperCls = cls as Class<out IRpcTarget>
        this.wrapperCls.put(wrapperCls, value)
    }

    override fun end() {}

    fun getWrapperId(cls: Class<out IRpcTarget>): Int {
        val id = wrapperCls[cls]
        Preconditions.checkNotNull(id, "Wrapper class %s is not registered", cls)
        return id!!
    }

    fun createWrapperFromId(id: Int): IRpcTarget {
        val cls = wrapperCls.inverse()[id]
        Preconditions.checkNotNull(cls, "Can't find class for id %s", id)

        try {
            return cls!!.newInstance()
        } catch (e: Exception) {
            throw Throwables.propagate(e)
        }

    }
}