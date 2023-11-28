package me.wanttobee.wtbmGameLib

interface IWindowObserver {
    fun onWindowResize(newWidth: Int, newHeight: Int) {}
    fun onWindowFocus(isFocused: Boolean) {}
}