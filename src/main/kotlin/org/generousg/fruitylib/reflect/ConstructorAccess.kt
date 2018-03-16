package org.generousg.fruitylib.reflect

import org.generousg.fruitylib.serializable.IInstanceFactory
import org.generousg.fruitylib.util.SneakyThrower
import java.lang.reflect.Constructor


class ConstructorAccess<T>(private val ctor: Constructor<out T>) : IInstanceFactory<T> {
    companion object {
        fun <T> create(cls: Class<out T>): ConstructorAccess<T> {
            try {
                val c = cls.getConstructor()
                return ConstructorAccess(c)
            } catch (t: Throwable) {
                throw SneakyThrower.sneakyThrow(t)
            }
        }
    }

    class ConstructorAccessException : RuntimeException {
        companion object {
            private fun createMessage(ctor: Constructor<*>) = "Failed to create object of ${ctor.declaringClass} via constructor $ctor"
        }

        constructor(ctor: Constructor<*>) : super(createMessage(ctor))
        constructor(ctor: Constructor<*>, cause: Throwable) : super(createMessage(ctor), cause)
    }

    init {
        ReflectionLog.logLoad(ctor)
        ctor.isAccessible = true
    }

    override fun create(): T {
        try {
            return ctor.newInstance()
        } catch (t: Throwable) {
            throw ConstructorAccessException(ctor, t)
        }
    }
}