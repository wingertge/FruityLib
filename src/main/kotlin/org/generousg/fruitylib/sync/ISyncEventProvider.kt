package org.generousg.fruitylib.sync

import org.generousg.fruitylib.util.events.Event


interface ISyncEventProvider {
    val outboundSyncEvent: Event<SyncMap.SyncEvent>
    val inboundSyncEvent: Event<SyncMap.SyncEvent>
}
