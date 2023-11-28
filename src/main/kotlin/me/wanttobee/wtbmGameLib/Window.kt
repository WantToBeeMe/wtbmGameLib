package me.wanttobee.wtbmGameLib

import me.wanttobee.wtbmGameLib.imGui.ImGuiController
import me.wanttobee.wtbmGameLib.input.Keyboard
import me.wanttobee.wtbmGameLib.input.Mouse
import org.joml.Vector3f
import org.joml.Vector4i
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_VERSION
import org.lwjgl.opengl.GL11.glGetString
import org.lwjgl.system.MemoryUtil
import java.util.*


object Window {
    private var glfwWindow : Long? = null
    private var fullScreen = false;
    private var width : Int = 900;
    private var height : Int = 600;
    private var backgroundColor : Vector3f = Vector3f()
    private var quickExitKey = -1;
    private var dt = 1f/60f;

    fun setColor(color : Vector3f){
        backgroundColor = color
    }
    fun setQuickExitKey(key: Int){
        quickExitKey = key
    }

    private val observers = mutableListOf<IWindowObserver>()
    fun subscribe(observer: IWindowObserver) {
        observers.add(observer)
    }
    fun unSubscribe(observer: IWindowObserver) {
        observers.remove(observer)
    }

    private fun onWindowResize(w: Long, width:Int, height:Int ){
        this.width = width
        this.height = height
        GL11.glViewport(0, 0, width, height);
        val observersCopy = ArrayList(observers)
        observersCopy.forEach {observer ->
            observer.onWindowResize(width, height)
        }
        // GuiMaster.resize(width,height)
        ImGuiController.windowResize(width.toFloat(), height.toFloat())
    }


    fun enableVSync(enable: Boolean){
        glfwSwapInterval(if(enable) GLFW_TRUE else GLFW_FALSE)
    }
    fun setMouseMode(captured: Boolean, hidden: Boolean) {
        glfwSetInputMode(glfwWindow!!, GLFW_CURSOR,
            when {
                captured && hidden -> GLFW_CURSOR_DISABLED
                captured && !hidden -> GLFW_CURSOR_CAPTURED
                !captured && hidden -> GLFW_CURSOR_HIDDEN
                else -> GLFW_CURSOR_NORMAL
            }
        )
    }

    fun enableFullscreen(enable: Boolean) {
        fullScreen = enable;
        glfwSetWindowAttrib(glfwWindow!!, GLFW_DECORATED, if(enable) GLFW_FALSE else GLFW_TRUE)
        glfwSetWindowAttrib(glfwWindow!!, GLFW_RESIZABLE, if (enable) GLFW_FALSE else GLFW_TRUE)

        val monitor = getMonitorRectangle()
        if (enable) {
            glfwSetWindowPos(glfwWindow!!, monitor.x, monitor.y);
            glfwSetWindowSize(glfwWindow!!, monitor.z, monitor.w);
        } else {
            val windowWidth = (monitor.z * 0.8f).toInt();
            val windowHeight = (monitor.w * 0.8f).toInt();
            glfwSetWindowPos(glfwWindow!!,
                monitor.z / 2 - windowWidth / 2 + monitor.x,
                monitor.w / 2 - windowHeight / 2 + monitor.y);
            glfwSetWindowSize(glfwWindow!!, windowWidth, windowHeight);
        }
    }

    private fun setFocus(w : Long, focus : Boolean){
        this.setFocus(focus)
    }
    fun setFocus(focus : Boolean){
        val observersCopy = ArrayList(observers)
        observersCopy.forEach {observer ->
            observer.onWindowFocus(focus)
        }
    }

    fun setWindowSize(width: Int, height: Int){
        if(fullScreen) return
        val monitor = getMonitorRectangle()
        glfwSetWindowPos(glfwWindow!!,
            monitor.z / 2 - width / 2 + monitor.x,
            monitor.w / 2 - height / 2 + monitor.y);
        glfwSetWindowSize(glfwWindow!!, width, height)
    }
    fun setWindowSize(percentage: Float){
        if(fullScreen) return
        val monitor = getMonitorRectangle()
        val newWidth = (percentage * monitor.z).toInt()
        val newHeight = (percentage * monitor.w).toInt()
        glfwSetWindowPos(glfwWindow!!,
            monitor.z / 2 - newWidth / 2 + monitor.x,
            monitor.w / 2 - newHeight / 2 + monitor.y);
        glfwSetWindowSize(glfwWindow!!, newWidth, newHeight);
    }

    private fun getMonitorRectangle() : Vector4i{
        // this way we can also make it, so you can toggle between window you want to use
        // val monitors = glfwGetMonitors() //it returns a pointer buffer (kinda like an array)
        // val monitor = if (monitors != null && monitors.remaining() > 0) monitors[0] else null
        // make sure the monitor isn't null before you do any actions
        val primaryMonitor = glfwGetPrimaryMonitor()
        val videoMode = glfwGetVideoMode(primaryMonitor)
        val screenWidth = videoMode!!.width();
        val screenHeight = videoMode.height();
        val monitorLeft = IntArray(1)
        val monitorTop = IntArray(1)
        glfwGetMonitorPos(primaryMonitor, monitorLeft, monitorTop);
        return Vector4i(monitorLeft[0],monitorTop[0], screenWidth,screenHeight)
    }

    fun initWindow(title: String, width:Int? = null , height:Int? = null){
        // setup an error callback
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW settings/hints
        // (before creating, creation is going to use these hints)
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE)

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE)
        // this makes it possible to use deprecated openGl stuff, that's because ImGui, because ImGui old

        // Creating the window
        val monitor = getMonitorRectangle()
        glfwWindow = glfwCreateWindow(
            width ?: this.width,
            height ?: this.height,
            title, MemoryUtil.NULL, MemoryUtil.NULL)
        glfwSetWindowPos(glfwWindow!!,
            monitor.z / 2 - (width ?: this.width) / 2 + monitor.x,
            monitor.w / 2 - (height ?: this.height) / 2 + monitor.y);

        glfwSetCursorPosCallback(glfwWindow!!, Mouse::mousePosCallback)
        glfwSetMouseButtonCallback(glfwWindow!!, Mouse::mouseButtonCallback)
        glfwSetScrollCallback(glfwWindow!!, Mouse::mouseScrollCallback)
        glfwSetKeyCallback(glfwWindow!!, Keyboard::keyCallback)
        glfwSetFramebufferSizeCallback(glfwWindow!!, this::onWindowResize)
        glfwSetWindowFocusCallback(glfwWindow!!, this::setFocus)

        glfwMakeContextCurrent(glfwWindow!!)
        enableVSync(true)

        if (glfwRawMouseMotionSupported()) glfwSetInputMode(
            glfwWindow!!,
            GLFW_RAW_MOUSE_MOTION,
            GLFW_TRUE
        );

        // make window visible again
        glfwShowWindow(glfwWindow!!)

        /* This line is critical for LWJGL's interoperation with GLFW's
        OpenGL context, or any context that is managed externally.
        LWJGL detects the context that is current in the current thread,
        creates the GLCapabilities instance and makes the OpenGL
         bindings available for use.*/
        GL.createCapabilities();

        //makes sure that everyone who is dependent on the right window size gets good initialized
        val widthBuffer = IntArray(1)
        val heightBuffer= IntArray(1)
        glfwGetWindowSize(glfwWindow!!, widthBuffer, heightBuffer)
        onWindowResize(glfwWindow!!, widthBuffer[0], heightBuffer[0] )
        Logger.logInfo("with openGL version: ${glGetString(GL_VERSION)}")
        ImGuiController.init(glfwWindow!!)
    }

    fun windowLoop( action: () -> Unit ){
        var beginTime = glfwGetTime().toFloat();
        var endTime = 0f;

        // Game.init()
        // Game.start()
        Mouse.setWindow(glfwWindow!!)
        while (!glfwWindowShouldClose(glfwWindow!!)){
            if(Keyboard.isKeyPressed(quickExitKey)){
                glfwSetWindowShouldClose(glfwWindow!!, true)
            }
            GL11.glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1f)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

            action.invoke()
            ImGuiController.render()
            Mouse.reset()

            glfwSwapBuffers(glfwWindow!!)
            glfwPollEvents()

            endTime= glfwGetTime().toFloat()
            dt = endTime - beginTime
            beginTime = endTime
        }
        destroy()
    }

    private fun destroy() {
        //if you created custom cursors, then also destroy those for extra bonus points
        //for (mouseCursor in mouseCursors) {
        //    GLFW.glfwDestroyCursor(mouseCursor)
        //}
        Callbacks.glfwFreeCallbacks(glfwWindow!!)
        glfwDestroyWindow(glfwWindow!!)
        glfwTerminate()
        Objects.requireNonNull(
            glfwSetErrorCallback(null)
        )?.free()
    }

    fun getDeltaTime() : Float{
        return dt
    }
}

// an alies so you don't have to type 'Window.' as a prefix
// not needed at all, but I like it >:)
fun getDeltaTime() : Float{
    return Window.getDeltaTime()
}
