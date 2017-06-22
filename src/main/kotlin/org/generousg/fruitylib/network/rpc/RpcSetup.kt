package org.generousg.fruitylib.network.rpc

import com.google.common.base.Preconditions
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState
import org.generousg.fruitylib.network.IdSyncManager
import org.objectweb.asm.Type
import util.TypeRW


class RpcSetup internal constructor() {
    companion object {
        val ID_FIELDS_SEPARATOR: Char = ';'
    }

    private var currentMethodId = 0
    private var currentWrapperId = 0

    private val methodsStoreBuilder = IdSyncManager.instance.value.createDataStore("rpc_methods", String::class.java, Int::class.java)
    private val targetsStoreBuilder = IdSyncManager.instance.value.createDataStore("rpc_targets", String::class.java, Int::class.java)

    init {
        methodsStoreBuilder.setDefaultKeyReaderWriter()
        methodsStoreBuilder.setValueReaderWriter(TypeRW.VLI_SERIALIZABLE)

        targetsStoreBuilder.setDefaultKeyReaderWriter()
        targetsStoreBuilder.setValueReaderWriter(TypeRW.VLI_SERIALIZABLE)
    }

    fun registerInterface(intf: Class<*>): RpcSetup {
        require(Loader.instance().isInState(LoaderState.PREINITIALIZATION)) { "This method can only be called in pre-initialization state" }
        require(intf.isInterface) { "Class $intf is not interface" }

        for(method in intf.methods) {
            if(method.isAnnotationPresent(RpcIgnore::class.java)) continue
            require(method.returnType == Unit::class.java) { "RPC methods cannot have return type (method = $method)" }
            MethodParamsCodec.create(method).validate()

            val desc = Type.getMethodDescriptor(method)
            val entry = method.declaringClass.name + ID_FIELDS_SEPARATOR + method.name + ID_FIELDS_SEPARATOR + desc

            if(!methodsStoreBuilder.isRegistered(entry)) methodsStoreBuilder.addEntry(entry, currentMethodId++)
        }
        return this
    }

    fun registerTargetWrapper(wrapperCls: Class<out IRpcTarget>): RpcSetup {
        Preconditions.checkState(Loader.instance().isInState(LoaderState.PREINITIALIZATION), "This method can only be called in pre-initialization state")
        targetsStoreBuilder.addEntry(wrapperCls.name, currentWrapperId++)
        return this
    }

    fun finish(methodRegistry: MethodIdRegistry, wrapperRegistry: TargetWrapperRegistry) {
        methodsStoreBuilder.addVisitor(methodRegistry)
        methodsStoreBuilder.register()

        targetsStoreBuilder.addVisitor(wrapperRegistry)
        targetsStoreBuilder.register()
    }
}