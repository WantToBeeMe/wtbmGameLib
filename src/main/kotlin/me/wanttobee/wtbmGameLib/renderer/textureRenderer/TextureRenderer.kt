package me.wanttobee.wtbmGameLib.renderer.textureRenderer

import me.wanttobee.wtbmGameLib.renderer.Camera
import me.wanttobee.wtbmGameLib.renderer.IRenderer
import me.wanttobee.wtbmGameLib.renderer.Shader
import me.wanttobee.wtbmGameLib.renderer.Texture2D
import me.wanttobee.wtbmGameLib.renderer.dynamicRenderer.DynamicRenderer
import org.joml.Matrix4f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45.*

object TextureRenderer : IRenderer<TextureRenderProgram> {
    override var blendOn: Boolean = false
    override var depthTestOn: Boolean = false

    private val DEFAULT_SHADER = Shader(null,null)
    override var currentShader: Shader = DEFAULT_SHADER
    private val DEFAULT_BATCH = TextureRenderProgram(intArrayOf(3,4,2,1), 32)
    override var currentBatch: TextureRenderProgram = DEFAULT_BATCH;

    override fun startDrawing() {
        glUseProgram(currentShader.shaderProgramID)
    }
    override fun endDrawing() {
        render()
        if(currentShader != DEFAULT_SHADER)
            currentShader = DEFAULT_SHADER
        if(currentBatch != DEFAULT_BATCH)
            currentBatch = DEFAULT_BATCH
    }

    // change the shader without changing the current batch (null if change back to default)
    override fun changeProgram(shaderProgram: Shader?) {
        render() // render the all the shit you just did before changing the program
        // will change whatever you just added to the batch in something you might not want,
        // you added it to the previous program, not this one, so it should be handled by the previous program
        currentShader = shaderProgram ?: DEFAULT_SHADER
        glUseProgram(currentShader.shaderProgramID)
    }

    // changing the shader and the batch. set second parameter to desired batch,
    // or to null if you want the batch to go back to default (stays unchanged if it was already default)
    override fun changeProgram(shaderProgram: Shader?, batchProgram: TextureRenderProgram?) {
        changeProgram(shaderProgram)
        currentBatch = if(batchProgram == null){
            if(currentBatch == DEFAULT_BATCH) return
            DEFAULT_BATCH
        } else
            batchProgram
    }

    private fun render(){
        DynamicRenderer.uploadMat4f("uProjMtx", Camera.current.getProjectionMatrix())
        DynamicRenderer.uploadMat4f("uViewMtx", Camera.current.getViewMatrix())

        currentBatch.render()
    }



    fun drawTexture(texture2D: Texture2D, source: Vector4f, dest: Vector4f){
        currentBatch.drawTexture(texture2D,source,dest)
    }

    fun drawTexture(texture2D: Texture2D, dest: Vector4f){
        this.drawTexture(texture2D,
            Vector4f(0f,0f,1f,1f),
            dest
        )
    }


}