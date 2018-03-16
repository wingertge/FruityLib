package org.generousg.fruitylib.network.rpc

import com.google.common.collect.HashBiMap
import org.apache.commons.lang3.ClassUtils
import org.generousg.fruitylib.datastore.IDataVisitor
import org.objectweb.asm.Type
import java.lang.reflect.Method


class MethodIdRegistry : IDataVisitor<String, Int> {
    private val registeredInterfaces = hashSetOf<Class<*>>()
    private val methodIds = HashBiMap.create<Method, Int>()

    fun isClassRegistered(cls: Class<*>) = registeredInterfaces.contains(cls)
    fun methodToId(method: Method): Int {
        val id = methodIds[method]
        requireNotNull(id) { "Method $method is ignored or not registered" }
        return id as Int
    }

    fun idToMethod(id: Int): Method {
        val method = methodIds.inverse()[id]
        requireNotNull(method) { "Unregistered method id $id" }
        return method as Method
    }

    override fun begin(size: Int) {
        registeredInterfaces.clear()
        methodIds.clear()
    }

    @Throws(Exception::class)
    private fun identifyMethod(methodDesc: String): Method {
        val parts = methodDesc.split(RpcSetup.ID_FIELDS_SEPARATOR, limit = 3)
        require(parts.size == 3) { "Method descriptor has ${parts.size} fields" }
        val declaringCls = Class.forName(parts[0])

        val method = org.objectweb.asm.commons.Method(parts[1], parts[2])

        val argTypes = method.argumentTypes
        val argCls = convertTypesToClasses(argTypes, declaringCls.classLoader)

        try {
            return declaringCls.getMethod(method.name, *argCls)
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("Can't find method, class $declaringCls has methods ${declaringCls.methods}", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(ClassNotFoundException::class)
    private fun convertTypesToClasses(argTypes: Array<Type>, loader: ClassLoader): Array<Class<*>> {
        val argCls = arrayOfNulls<Class<*>>(argTypes.size)
        for (i in argCls.indices)
            argCls[i] = ClassUtils.getClass(loader, argTypes[i].className, false)
        return argCls as Array<Class<*>>
    }

    override fun entry(key: String, value: Int) {
        val method: Method
        try {
            method = identifyMethod(key)
        } catch (e: Throwable) {
            throw IllegalArgumentException(String.format("Malformed entry '%s' in method id %d", key, value), e)
        }

        MethodParamsCodec.create(method).validate()

        methodIds.put(method, value)

        val declaringClass = method.declaringClass
        registeredInterfaces.add(declaringClass)
    }

    override fun end() {}
}