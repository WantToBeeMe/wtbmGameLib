package me.wanttobee.wtbmGameLib.input

interface IMouseObserver {
    fun onMouseScroll(xPos : Double, yPos: Double, xScroll: Double, yScroll : Double) {}
    fun onMouseMove(xPos : Double, yPos: Double, dx : Double, dy : Double) {}
    fun onMouseClick(xPos : Double, yPos: Double, button : Int) {}
    fun onMouseRelease(xPos : Double, yPos: Double, button : Int) {}
}