package org.generousg.fruitylib.sync

import com.google.common.base.Preconditions
import com.google.common.collect.Lists
import org.generousg.fruitylib.util.FieldsSelector
import org.generousg.fruitylib.util.Log


class SyncObjectScanner : FieldsSelector() {

    override fun listFields(cls: Class<*>): List<FieldEntry> {
        var cls1 = cls
        val result = Lists.newArrayList<FieldEntry>()

        while (cls1 != Any::class.java) {
            cls1.declaredFields
                    .filter { ISyncableObject::class.java.isAssignableFrom(it.type) }
                    .mapTo(result) { FieldEntry(it, 0) }

            cls1 = cls1.superclass
        }

        return result
    }

    fun registerAllFields(map: SyncMap<*>, target: Any) {
        for (field in getFields(target.javaClass)) {
            var obj: ISyncableObject
            try {
                obj = field.get(target) as ISyncableObject
                Preconditions.checkNotNull(obj, "Null field value")
            } catch (e: Exception) {
                obj = DummySyncableObject.instance.value
                Log.severe(e, "Exception while registering synced field '$field' of object '$target'")
            }

            val fieldName = field.name
            map.put(fieldName, obj)
        }
    }

    companion object {
        val instance by lazy { SyncObjectScanner() }
    }
}