package me.wanttobee.wtbmGameLib.renderer.dynamicRenderer

import me.wanttobee.wtbmGameLib.renderer.Camera
import me.wanttobee.wtbmGameLib.renderer.IRenderer
import me.wanttobee.wtbmGameLib.renderer.Shader
import me.wanttobee.wtbmGameLib.renderer.Texture2D
import org.joml.*
import org.lwjgl.opengl.GL45.*


// TODO: Camera
//       texture batching (find out how much your grapics card can have, 8 / 16 / 32)
//       textures with reserveBatchSpot (that if the texture already exists, it shouldn't be a problem)
//       drawRectangle(),  drawTexture()
//       how do way say an batch will react on drawTexture with a different batch size (probably just make the right one first)
//       how do you rotate an image ? prob matrix before you even start drawing right, anyway, drawRectanglePro(... , 30deg)

// This Dynamic Renderer is one of the different render types that are created (currently this is a lie and there is only 1, but i am planning to create more)
// Each renderer has its pro's and cons, so you have to decide which renderer is best suited for your game
// This DynamicRenderer is more of a "draw everywhere whenever you want" style, just call a draw method (or make your won draw call method)
// this then will draw it (with the assigned shader and/or batch )
object DynamicRenderer : IRenderer<DynamicRenderProgram> {
    private val DEFAULT_SHADER = Shader(null,null)
    override var currentShader: Shader = DEFAULT_SHADER

    private val DEFAULT_BATCH = DynamicRenderProgram(intArrayOf(3,4,2,1), true) //3=position   4=color   2=UV texture cords
    override var currentBatch : DynamicRenderProgram = DEFAULT_BATCH

    override var blendOn: Boolean = false
    override var depthTestOn: Boolean = false

    //glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
    override fun startDrawing(){
        glUseProgram(currentShader.shaderProgramID)
    }

    // change the shader without changing the current batch (null if change back to default)
    override fun changeProgram(shaderProgram: Shader?){
        render() // render the all the shit you just did before changing the program
        // will change whatever you just added to the batch in something you might not want,
        // you added it to the previous program, not this one, so it should be handled by the previous program
        currentShader = shaderProgram ?: DEFAULT_SHADER
        glUseProgram(currentShader.shaderProgramID)
    }

    // changing the shader and the batch. set second parameter to desired batch,
    // or to null if you want the batch to go back to default (stays unchanged if it was already default)
    override fun changeProgram(shaderProgram: Shader?, batchProgram: DynamicRenderProgram?){
        changeProgram(shaderProgram)
        currentBatch = if(batchProgram == null){
            if(currentBatch == DEFAULT_BATCH) return
            DEFAULT_BATCH
        } else
            batchProgram
    }

    override fun endDrawing(){
        render()
        if(currentShader != DEFAULT_SHADER)
            currentShader = DEFAULT_SHADER
        if(currentBatch != DEFAULT_BATCH)
            currentBatch = DEFAULT_BATCH
    }

    fun reserveBatchSpot(vertices : Int, elements: Int, textures: Int = 0) {
        currentBatch.reserveBatchSpot(vertices,elements, textures)
    }
    fun addTexture(texture: Texture2D) : Int{
        return currentBatch.addTexture(texture)
    }
    fun addVertex(vertex: FloatArray, textureID : Int = -1) : Int{
        return currentBatch.addVertex(vertex, textureID)
    }
    fun addTriangle(first: Int, second: Int, third: Int) : Int {
        return currentBatch.addTriangle(first,second,third)
    }

    private fun render(){
        uploadMat4f("uProjMtx", Camera.current.getProjectionMatrix())
        uploadMat4f("uViewMtx", Camera.current.getViewMatrix())

        currentBatch.render()
    }
}
