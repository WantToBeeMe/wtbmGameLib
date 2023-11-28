package me.wanttobee.wtbmGameLib

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

    private fun printLogInConsole(message: String, level: LogLevel, logOnce: Boolean) {
        if(logOnce){
            if(logOnceList.contains(message))
                return
            logOnceList.add(message)
        }
        val coloredMessage = when (level) {
            LogLevel.ERROR -> "\u001B[31m$message\u001B[0m"    // Red
            LogLevel.WARNING -> "\u001B[33m$message\u001B[0m"  // Yellow
            LogLevel.INFO -> "\u001B[34m$message\u001B[0m"     // Blue
            LogLevel.DEBUG -> "\u001B[0m$message"             // White
            else -> message
        }
        if (level.ordinal >= currentLogLevel.ordinal || currentLogLevel == LogLevel.ALL) {
            println("\u001B[37m$level:\u001B[0m $coloredMessage")
        }
    }

    fun logError(message: String, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.ERROR,logOnce)
    }

    fun logWarning(message: String, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.WARNING,logOnce)
    }

    fun logInfo(message: String, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.INFO,logOnce)
    }


    fun logDebug(message: String, logOnce: Boolean = false) {
        printLogInConsole(message, LogLevel.DEBUG,logOnce)
    }
}

// log is an easy alies for the debug log, because that one will be a lot
// and doing this i hope everyone (mostly me) will be motivated to use this instead of the
// regular println() so it can be regulated better
fun log(message: Any, logOnce: Boolean = false) {
    Logger.logDebug(message.toString(),logOnce)
}