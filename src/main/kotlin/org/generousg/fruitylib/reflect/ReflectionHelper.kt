package org.generousg.fruitylib.reflect

import com.google.common.base.Strings
import com.google.common.base.Throwables
import java.lang.reflect.Field
import java.util.*


class ReflectionHelper {
    companion object {
        class MethodNotFoundException(cls: Class<*>, names: Array<out String>, args: Array<Class<*>>) : RuntimeException("Method not found: $cls.${Arrays.toString(names)}${Arrays.toString(args)}")
        class FieldNotFoundException(cls: Class<*>, vararg names: String) : RuntimeException("Field not found: $cls.${Arrays.toString(names)}")

        fun getField(cls: Class<*>, vararg fields: String): Field {
            for(field in fields) {
                var current: Class<*>? = cls
                while(current != null) {
                    try {
                        val f = current.getDeclaredField(field)
                        f.isAccessible = true
                        ReflectionLog.logLoad(f)
                        return f
                    } catch (e: NoSuchFieldException) { } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                    current = current.superclass
                }
            }

            throw FieldNotFoundException(cls, *fields)
        }

        fun getClass(className: String): Class<*>? {
            if (Strings.isNullOrEmpty(className)) return null
            try {
                val cls = Class.forName(className)
                ReflectionLog.logLoad(cls)
                return cls
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
    }
}