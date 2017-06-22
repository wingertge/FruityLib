package org.generousg.fruitylib.network.event

import com.google.common.base.Preconditions
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState
import org.generousg.fruitylib.datastore.DataStoreBuilder
import org.generousg.fruitylib.datastore.IDataVisitor
import org.generousg.fruitylib.network.IdSyncManager
import util.TypeRW


class NetworkEventManager private constructor() {

    class RegistrationContext internal constructor() {
        private var currentId = 0

        private val builder: DataStoreBuilder<String, Int> = IdSyncManager.instance.value.createDataStore("events", String::class.java, Int::class.java)

        init {

            this.builder.setDefaultKeyReaderWriter()
            this.builder.setValueReaderWriter(TypeRW.VLI_SERIALIZABLE)
        }

        fun register(cls: Class<out NetworkEvent>): RegistrationContext {
            Preconditions.checkState(Loader.instance().isInState(LoaderState.PREINITIALIZATION), "This method can only be called in pre-initialization state")

            builder.addEntry(cls.name, currentId++)
            return this
        }

        internal fun register(eventIdVisitor: IDataVisitor<String, Int>) {
            builder.addVisitor(eventIdVisitor)
            builder.register()
        }
    }

    private val registry = NetworkEventRegistry()

    private val dispatcher = NetworkEventDispatcher(registry)

    private var registrationContext: RegistrationContext? = RegistrationContext()

    fun startRegistration(): RegistrationContext {
        Preconditions.checkState(Loader.instance().isInState(LoaderState.PREINITIALIZATION), "This method can only be called in pre-initialization state")
        return registrationContext!!
    }

    fun finalizeRegistration() {
        registrationContext!!.register(registry)
        registrationContext = null
    }

    fun dispatcher(): NetworkEventDispatcher {
        return dispatcher
    }

    companion object {

        val instance = lazy { NetworkEventManager() }
    }
}