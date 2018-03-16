package org.generousg.fruitylib.reflect

import org.generousg.fruitylib.util.Log


class SafeClassLoad(val clsName: String) {
    private var loaded: Class<*>? = null

    fun load() {
        if (loaded == null) loaded = ReflectionHelper.getClass(clsName)
    }

    fun get(): Class<*> {
        load()
        return loaded!!
    }

    @JvmOverloads fun tryLoad(silent: Boolean = true): Boolean {
        try {
            load()
            return true
        } catch (t: Throwable) {
            if (!silent) Log.warn(t, "Loading class %s failed", clsName)
            return false
        }

    }

    override fun toString(): String {
        return "delayed " + clsName
    }

    companion object {

        fun create(clsName: String): SafeClassLoad {
            return SafeClassLoad(clsName)
        }
    }
}