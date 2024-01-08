package me.wanttobee.wtbmGameLib.renderer

interface IRenderProgram {

    fun clear()
    fun render()
    fun printCurrentState(newLines: Boolean)
}