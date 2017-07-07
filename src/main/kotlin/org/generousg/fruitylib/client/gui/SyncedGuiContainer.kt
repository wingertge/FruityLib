package org.generousg.fruitylib.client.gui

import org.generousg.fruitylib.client.gui.logic.IValueUpdateAction
import org.generousg.fruitylib.client.gui.logic.ValueUpdateDispatcher
import org.generousg.fruitylib.container.ContainerBase
import org.generousg.fruitylib.sync.ISyncMapProvider
import org.generousg.fruitylib.sync.SyncMap


abstract class SyncedGuiContainer<out T : ContainerBase<ISyncMapProvider>>(container: T, width: Int, height: Int, name: String) : BaseGuiContainer<T>(container, width, height, name) {
    private var listenerLambda: ((SyncMap.SyncEvent)->Unit)? = null
    private val dispatcher = lazy {
        val tempDispatcher = ValueUpdateDispatcher()
        listenerLambda = { tempDispatcher.trigger(it.changes) }
        container.owner.syncMap.receivedSyncEvent += { tempDispatcher.trigger(it.changes) }
        return@lazy tempDispatcher
    }

    fun addSyncUpdateListener(action: IValueUpdateAction) = dispatcher.value.addAction(action)
    override fun onGuiClosed() {
        super.onGuiClosed()
        if(listenerLambda != null) container.owner.syncMap.receivedSyncEvent -= listenerLambda as ((SyncMap.SyncEvent)->Unit)
    }
}