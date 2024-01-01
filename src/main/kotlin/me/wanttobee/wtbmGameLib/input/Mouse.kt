package me.wanttobee.wtbmGameLib.input

import org.lwjgl.glfw.GLFW.*
import java.util.ArrayList

//Description: a class handles the callbacks for the mouse
//------
//Usage:  create an object that has one of the IMouseObserver inherited
//then you can subscribe for those subjects to start reacting to any mouse callbacks,
// or you can use the getters, though that one is not preferable

object Mouse {
    const val LEFT = 0
    const val RIGHT = 1
    const val MIDDLE = 2

    private var scrollX: Double = 0.0
    private var scrollY: Double = 0.0
    private var xPos: Double = 0.0
    private var yPos: Double = 0.0
    private var lastY: Double = 0.0
    private var lastX: Double = 0.0
    private val mouseButtonPressed =  BooleanArray(3)

    private var glfwWindow : Long = 0
    private var hide = false
    private var ignoreX : Double = 0.0
    private var ignoreY : Double = 0.0

    private val mouseObserver : MutableList<IMouseObserver> = mutableListOf()
    fun subscribe(imo : IMouseObserver){ if(mouseObserver.contains(imo))return;mouseObserver.add(imo) }
    fun unSubscribe(imo : IMouseObserver) : Boolean{ return mouseObserver.remove(imo) }


    fun reset(){
        scrollX = 0.0
        scrollY = 0.0
        lastX = xPos
        lastY = yPos
    }

    fun setHide(h : Boolean){
        hide = h;

      if(glfwWindow > 0 ){
          if(hide) glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)
          else {
              glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_CAPTURED)
              ignoreX = 0.0
              ignoreY = 0.0
          };
      };
    }

    fun setWindow(window : Long){
        glfwWindow = window //don't use these once's yet, are broken... sadly
    }

    fun mousePosCallback(window: Long, xpos: Double, ypos: Double){
        if(glfwWindow > 0 && hide){
            val height = IntArray(1)
            val width = IntArray(1)
            glfwGetWindowSize(glfwWindow,width,height)
            glfwSetCursorPos(glfwWindow,width[0]/2.0,height[0]/2.0);
            ignoreX = width[0]/2.0 - xpos
            ignoreY = height[0]/2.0 - ypos
        }
        lastX = xPos  + ignoreX;
        lastY = yPos  + ignoreY;
        xPos = xpos + ignoreX;
        yPos = ypos + ignoreY;
        val observers = ArrayList(mouseObserver)
        observers.forEach {observer ->
            observer.onMouseMove(xpos, ypos, lastX -xpos, lastY -ypos)
        }

    }
    fun mouseButtonCallback(window : Long, button : Int, action: Int, mods : Int ){
        if(button >= mouseButtonPressed.size) return
        if(action == GLFW_PRESS){
            mouseButtonPressed[button] = true;
            val observers = ArrayList(mouseObserver)
            observers.forEach {observer ->
                observer.onMouseClick(xPos, yPos, button)
            }
        }
        else if(action == GLFW_RELEASE){
            mouseButtonPressed[button] = false;
            val observers = ArrayList(mouseObserver)
            observers.forEach {observer ->
                observer.onMouseRelease(xPos, yPos, button)
            }
        }
    }
    fun mouseScrollCallback(window : Long, xOffset: Double, yOffset:Double){
        scrollX = xOffset
        scrollY = yOffset
        val observers = ArrayList(mouseObserver)
        observers.forEach {observer ->
            observer.onMouseScroll(xPos, yPos, xOffset,yOffset)
        }
    }


    fun getX(): Float { return xPos.toFloat() }
    fun getY(): Float { return yPos.toFloat() }
    fun getDx(): Float { return (lastX - xPos).toFloat() }
    fun getDy(): Float { return (lastY - yPos).toFloat() }
    fun getScrollX(): Float { return scrollX.toFloat() }
    fun getScrollY(): Float { return scrollY.toFloat() }
    fun mouseButtonPressed(button : Int) : Boolean {
        if(button > mouseButtonPressed.size) return false
        return mouseButtonPressed[button]
    }

}