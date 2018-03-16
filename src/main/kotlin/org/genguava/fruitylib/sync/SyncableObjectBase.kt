package org.genguava.fruitylib.sync


abstract class SyncableObjectBase : ISyncableObject {

    protected var dirty = false

    override fun isDirty(): Boolean {
        return dirty
    }

    override fun markClean() {
        dirty = false
    }

    override fun markDirty() {
        dirty = true
    }
}