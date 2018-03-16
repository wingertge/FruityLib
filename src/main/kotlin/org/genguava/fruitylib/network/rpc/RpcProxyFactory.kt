package org.genguava.fruitylib.network.rpc

import com.google.common.base.Preconditions
import org.apache.commons.lang3.ArrayUtils
import org.genguava.fruitylib.network.senders.IPacketSender
import java.lang.reflect.Proxy


class RpcProxyFactory(private val registry: MethodIdRegistry) {
    @Suppress("UNCHECKED_CAST")
    fun <T> createProxy(loader: ClassLoader, sender: IPacketSender, wrapper: IRpcTarget, mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        val allInterfaces = ArrayUtils.add(extraIntf, mainIntf)

        for (intf in allInterfaces)
            Preconditions.checkState(registry.isClassRegistered(intf), "Class %s is not registered as RPC interface", intf)

        val proxy = Proxy.newProxyInstance(loader, allInterfaces) { _, method, args ->
            val call = RpcCall(wrapper, method, args)
            sender.sendMessage(call)
            null
        }

        return proxy as T
    }
}