package me.wanttobee.wtbmGameLib.renderer

import org.joml.Matrix4f
import me.wanttobee.wtbmGameLib.Logger
import org.joml.Vector3f

class Camera(
    private val isOrthographic: Boolean,
    private val fov: Float = 60f, // Field of view for perspective projection
    private val near: Float = 0.1f, // Near plane distance for perspective projection
    private val far: Float = 100f, // Far plane distance for perspective projection
    private val orthoLeft: Float = -1f, // Left coordinate for orthographic projection
    private val orthoRight: Float = 1f, // Right coordinate for orthographic projection
    private val orthoBottom: Float = -1f, // Bottom coordinate for orthographic projection
    private val orthoTop: Float = 1f // Top coordinate for orthographic projection
) {
    private val projectionMatrix = Matrix4f()
    private val viewMatrix = Matrix4f().identity().setLookAt(Vector3f(0f, 0f, 0f), Vector3f(0f, 0f, 1f), Vector3f(0f, 1f, 0f))

    companion object{
        val windowCamera = Camera(true )//, orthoLeft = 0f, orthoRight = 1280f, orthoBottom = 0f, orthoTop = 720f)
        var current = windowCamera
    }

    init {
        updateProjectionMatrix()
        if(near == 0.0f){
           Logger.logError("near plane cant be 0 for the camera", true)
        }
    }

    fun updateProjectionMatrix() {
        projectionMatrix.identity()

        if (isOrthographic)
            projectionMatrix.setOrtho(orthoLeft, orthoRight, orthoBottom, orthoTop, near, far)
        else projectionMatrix.setPerspective(fov, 1f, near, far)
    }

    fun getViewMatrix(): Matrix4f {
        return viewMatrix
    }

    fun getProjectionMatrix() : Matrix4f {
        return projectionMatrix
    }

}
