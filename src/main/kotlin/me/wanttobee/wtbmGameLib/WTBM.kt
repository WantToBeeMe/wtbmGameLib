package me.wanttobee.wtbmGameLib

object WTBM {
    var devMode = false
        private set

    fun setDevMode(enable: Boolean){
        devMode = enable
        if(enable)
            Logger.setLogLevel(Logger.LogLevel.DEBUG)
        else
            Logger.setLogLevel(Logger.LogLevel.ERROR)
    }

}