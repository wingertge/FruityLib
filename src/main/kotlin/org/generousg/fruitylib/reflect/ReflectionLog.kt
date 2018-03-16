package org.generousg.fruitylib.reflect

import org.apache.logging.log4j.LogManager
import org.objectweb.asm.Type
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method


class ReflectionLog {
    companion object {
        private val ENABLE_LOG = System.getProperty("fruitylib.logReflection", "false").toBoolean()
        private val logger = LogManager.getLogger("FruityLib-Reflection")
        private val stackInfo = Throwable()

        private fun findCaller(): String {
            return synchronized(stackInfo) {
                val stack = stackInfo.fillInStackTrace().stackTrace
                return@synchronized stack.map { it.className }.filterNot { it.startsWith("org.generousg.fruitylib.reflect.") }.firstOrNull() ?: "<invalid>"
            }
        }

        fun logLoad(cls: Class<*>) {
            if(ENABLE_LOG) logger.debug("###C ${findCaller()} ${cls.name}")
        }

        fun logLoad(ctor: Constructor<*>) {
            if(ENABLE_LOG) logger.debug("###I ${findCaller()} ${ctor.declaringClass.name} ${Type.getType(ctor).descriptor}")
        }

        fun logLoad(method: Method) {
            if(ENABLE_LOG) logger.debug("###M ${findCaller()} ${method.declaringClass.name} ${method.name} ${Type.getType(method).descriptor}")
        }

        fun logLoad(field: Field) {
            if(ENABLE_LOG) logger.debug("###F ${findCaller()} ${field.declaringClass.name} ${field.name} ${Type.getType(field.type).descriptor}")
        }
    }
}