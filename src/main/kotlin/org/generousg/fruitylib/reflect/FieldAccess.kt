package org.generousg.fruitylib.reflect

import java.lang.reflect.Field
import java.lang.reflect.Modifier


@Suppress("UNCHECKED_CAST")
open class FieldAccess<T> private constructor(val field: Field, log: Boolean = true) {
    class FieldAccessException : RuntimeException {
        companion object { fun createMessage(f: Field, action: String) = "Failed to $action field $f" }

        constructor(f: Field, action: String, cause: Throwable) : super(createMessage(f, action), cause)
        constructor(f:Field, action: String) : super(createMessage(f, action))
    }

    val isPublic get() = Modifier.isPublic(field.modifiers)
    val isPrivate get() = Modifier.isPrivate(field.modifiers)
    val isProtected get() = Modifier.isProtected(field.modifiers)
    val isFinal get() = Modifier.isFinal(field.modifiers)
    val isStatic get() = Modifier.isStatic(field.modifiers)
    val type: Class<T> get() = this.field.type as Class<T>

    init {
        if(log) ReflectionLog.logLoad(field)
        field.isAccessible = true
    }

    constructor(field: Field) : this(field, true)

    operator fun get(target: Any?): T {
        try {
            return field.get(target) as T
        } catch (t: Throwable) {
            throw FieldAccessException(field, "read", t)
        }
    }

    operator fun set(target: Any?, value: T) {
        try {
            field.set(target, value)
        } catch (t: Throwable) {
            throw FieldAccessException(field, "set", t)
        }
    }

    fun <C> cast(cls: Class<out C>): FieldAccess<C> {
        val fieldType = field.type
        if(!cls.isAssignableFrom(fieldType)) throw ClassCastException("$field cannot be used as field of type $cls")
        return this as FieldAccess<C>
    }

    companion object {
        fun <T> create(cls: Class<*>, vararg names: String): FieldAccess<T> {
            val f = ReflectionHelper.getField(cls, *names)
            return FieldAccess(f, false)
        }

        fun <T> create(f: Field) = FieldAccess<T>(f, true)
    }
}