package org.generousg.fruitylib.network.rpc

import com.google.common.base.Preconditions
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import org.generousg.fruitylib.network.Dispatcher
import org.generousg.fruitylib.network.ExtendedOutboundHandler
import org.generousg.fruitylib.network.senders.IPacketSender


class RpcCallDispatcher private constructor(): Dispatcher() {
    private object InstanceHolder { val INSTANCE = RpcCallDispatcher() }
    companion object {
        val instance = lazy { InstanceHolder.INSTANCE }
        val CHANNEL_NAME = "FruityLib|RPC"
    }

    val senders: Senders
    private val methodRegistry = MethodIdRegistry()
    private val targetRegistry = TargetWrapperRegistry()

    private var setup: RpcSetup? = RpcSetup()

    private val proxyFactory = RpcProxyFactory(methodRegistry)

    private val channels: Map<Side, FMLEmbeddedChannel>

    init {
        this.channels = NetworkRegistry.INSTANCE.newChannel(CHANNEL_NAME, RpcCallCodec(targetRegistry, methodRegistry), RpcCallInboundHandler())
        ExtendedOutboundHandler.install(this.channels)

        this.senders = Senders()
    }

    protected override fun getChannel(side: Side): FMLEmbeddedChannel {
        return channels[side]!!
    }

    fun startRegistration(): RpcSetup {
        Preconditions.checkState(Loader.instance().isInState(LoaderState.PREINITIALIZATION), "This method can only be called in pre-initialization neighborState")
        return setup!!
    }

    fun finishRegistration() {
        setup!!.finish(methodRegistry, targetRegistry)
        setup = null
    }

    fun <T> createProxy(wrapper: IRpcTarget, sender: IPacketSender, mainIntf: Class<out T>, vararg extraIntf: Class<*>): T {
        return proxyFactory.createProxy(javaClass.classLoader, sender, wrapper, mainIntf, *extraIntf)
    }
}