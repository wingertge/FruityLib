package org.generousg.fruitylib.util

import net.minecraftforge.fml.common.FMLLog
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger


object Log {
    private var logger: Logger = FMLLog.getLogger()

    private var stackInfo = Throwable()

    private fun getLogLocation(t: Throwable): String {
        val stack = t.stackTrace
        if(stack.size < 2) return ""
        val caller = stack[1]
        return "${caller.className}.${caller.methodName}(${caller.fileName}:${caller.lineNumber}): "
    }

    private fun logWithCaller(callerStack: Throwable, level: Level, format: String, vararg data: Any) {
        logger.log(level, getLogLocation(callerStack) + String.format(format, data))
    }

    fun log(level: Level, format: String, vararg data: Any) = logWithCaller(stackInfo.fillInStackTrace(), level, format, data)

    fun severe(format: String, vararg data: Any) = log(Level.ERROR, format, data)
    fun warn(format: String, vararg data: Any) = log(Level.WARN, format, data)
    fun info(format: String, vararg data: Any) = log(Level.INFO, format, data)
    fun debug(format: String, vararg data: Any) = log(Level.DEBUG, format, data)
    fun trace(format: String, vararg data: Any) = log(Level.TRACE, format, data)

    fun log(level: Level, ex: Throwable, format: String, vararg data: Any) = logger.log(level, String.format(format, data), ex)
    fun severe(ex: Throwable, format: String, vararg data: Any) = log(Level.ERROR, ex, format, data)
    fun warn(ex: Throwable, format: String, vararg data: Any) = log(Level.WARN, ex, format, data)
    fun info(ex: Throwable, format: String, vararg data: Any) = log(Level.INFO, ex, format, data)
}