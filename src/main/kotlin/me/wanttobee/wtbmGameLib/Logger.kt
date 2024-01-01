package me.wanttobee.wtbmGameLib

import java.io.PrintStream

object Logger {
    enum class LogLevel {
        ALL,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        NONE,
    }
    private val logOnceList: MutableList<String> = mutableListOf()
    private var currentLogLevel: LogLevel = LogLevel.ERROR
    fun setLogLevel(level: LogLevel) {
        currentLogLevel = level
    }

    private fun printLogInConsole(message: Any?, level: LogLevel, logOnce: Boolean) {
        val m: String = message.toString()
        if(logOnce){
            if(logOnceList.contains(m))
                return
            logOnceList.add(m)
        }
        val coloredMessage = when (level) {
            LogLevel.ERROR -> "\u001B[31m$m\u001B[0m"    // Red
            LogLevel.WARNING -> "\u001B[33m$m\u001B[0m"  // Yellow
            LogLevel.INFO -> "\u001B[34m$m\u001B[0m"     // Blue
            LogLevel.DEBUG -> "\u001B[0m$m"             // White
            else -> m
        }
        if (level.ordinal >= currentLogLevel.ordinal || currentLogLevel == LogLevel.ALL) {
            println("\u001B[37m$level:\u001B[0m $coloredMessage")
        }
    }

    fun logError(message: Any?, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.ERROR,logOnce)
    }

    fun logWarning(message: Any?, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.WARNING,logOnce)
    }

    fun logInfo(message: Any?, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.INFO,logOnce)
    }


    fun logDebug(message: Any?, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.DEBUG,logOnce)
    }
}

// log is an easy alies for the debug log, because that one will be a lot
// and doing this i hope everyone (mostly me) will be motivated to use this instead of the
// regular println() so it can be regulated better
fun log(message: Any?, logOnce: Boolean = false) {
    Logger.logDebug(message,logOnce)
}