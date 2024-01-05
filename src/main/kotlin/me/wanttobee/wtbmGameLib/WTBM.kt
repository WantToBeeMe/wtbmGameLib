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

    fun recommendedMainArgsConfig(args: Array<String>) {
        if (args.contains("-dev")) {
            setDevMode(true)
        }

        if (args.contains("-log")) {
            Logger.setLogLevel(Logger.LogLevel.INFO)
        }

        if (args.contains("-col")) {
            Logger.toggleColors()
        }
    }

}