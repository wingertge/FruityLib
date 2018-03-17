package org.generousg.fruitylib.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


object Log {
    private var logger: Logger = LogManager.getLogger("FruityLib")
    private var stackInfo = Throwable()
    private const val PREFIX = "[FruityLib] "

    private fun getLogLocation(t: Throwable): String {
        val stack = t.stackTrace
        if(stack.size < 2) return ""
        var caller = stack[1]
        if(caller.className == this.javaClass.name) caller = stack[2]
        val simpleCallerName = caller.className.substring(caller.className.lastIndexOf('.') + 1)
        return "$simpleCallerName.${caller.methodName}(${caller.fileName}:${caller.lineNumber}): "
    }

    private fun logWithCaller(callerStack: Throwable, level: Level, format: String, vararg data: Any) {
        logger.log(level, PREFIX + getLogLocation(callerStack) + String.format(format, data))
    }

    fun log(level: Level, format: String, vararg data: Any) = logWithCaller(stackInfo.fillInStackTrace(), level, format, data)

    fun severe(text: () -> String) {
        if(logger.isErrorEnabled) log(Level.ERROR, text())
    }
    fun warn(text: () -> String) {
        if(logger.isWarnEnabled) log(Level.WARN, text())
    }
    fun info(text: () -> String) {
        if(logger.isInfoEnabled) log(Level.INFO, text())
    }
    fun debug(text: () -> String) {
        if(logger.isDebugEnabled) log(Level.DEBUG, text())
    }
    fun trace(text: () -> String) {
        if(logger.isTraceEnabled) log(Level.TRACE, text())
    }

    fun log(level: Level, ex: Throwable, format: String, vararg data: Any) = logger.log(level, String.format(PREFIX + format, data), ex)
    fun severe(ex: Throwable, format: String, vararg data: Any) = log(Level.ERROR, ex, format, data)
    fun warn(ex: Throwable, format: String, vararg data: Any) = log(Level.WARN, ex, format, data)
    fun info(ex: Throwable, format: String, vararg data: Any) = log(Level.INFO, ex, format, data)

    fun setLogger(logger: Logger) {
        Log.logger = logger
    }
}