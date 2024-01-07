package me.wanttobee.wtbmGameLib.renderer.dynamicRendererr

import me.wanttobee.wtbmGameLib.log
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

object DynamicRenderer : IRenderer {
    private val DEFAULT_SHADER = Shader(null,null)
    override var currentShader: Shader = DEFAULT_SHADER

    private val DEFAULT_BATCH = RenderBatchProgram(intArrayOf(3,4,2,1), true) //3=position   4=color   2=UV texture cords
    override var currentBatch : RenderBatchProgram = DEFAULT_BATCH

    private var defaultBlendOn = false
    fun setDefaultBlend(on : Boolean){
        defaultBlendOn = on
    }
    private var blendOn = false
    fun toggleBlend(on : Boolean){
        // we do it this way because it will probably be i tiny bit faster to check if it has to be set before setting
        // (like, instead of enabling blend even though its enabled already. idk how that is done because I didn't look, but i can't imagine a single if check being less efficient then changing how the gpu is going to work)
        if(on && !blendOn){
            blendOn = true
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }
        else if(!on && blendOn){
            blendOn = false
            glDisable(GL_BLEND)
        }
    }

    private var defaultDepthTestOn = false
    fun setDefaultDepthTest(on : Boolean){
        defaultDepthTestOn = on
    }
    private var depthTestOn = false
    fun toggleDepthTest(on : Boolean){
        if(on && !depthTestOn){
            depthTestOn = true
            glEnable(GL_DEPTH_TEST)
            glDepthFunc(GL_LESS)
        }
        else if(!on && depthTestOn){
            depthTestOn = false
            glDisable(GL_DEPTH_TEST)
        }
    }

    //glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );

    fun startDrawing(){
        if(defaultBlendOn != blendOn) toggleBlend(defaultBlendOn)
        if(defaultDepthTestOn != depthTestOn) toggleDepthTest(defaultDepthTestOn)
        glUseProgram(currentShader.shaderProgramID)
    }

    // change the shader without changing the current batch (null if change back to default)
    fun changeProgram(shaderProgram: Shader?){
        render() // render the all the shit you just did before changing the program
        // will change whatever you just added to the batch in something you might not want,
        // you added it to the previous program, not this one, so it should be handled by the previous program
        currentShader = shaderProgram ?: DEFAULT_SHADER
        glUseProgram(currentShader.shaderProgramID)
    }

    // changing the shader and the batch. set second parameter to desired batch,
    // or to null if you want the batch to go back to default (stays unchanged if it was already default)
    fun changeProgram(shaderProgram: Shader?, batchProgram: RenderBatchProgram?){
        changeProgram(shaderProgram)
        currentBatch = if(batchProgram == null){
            if(currentBatch == DEFAULT_BATCH) return
            DEFAULT_BATCH
        } else
            batchProgram
    }

    fun endDrawing(){
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

    private val identity : Matrix4f = Matrix4f().identity()
    private fun render(){
        uploadMat4f("ProjMtx", identity)

        currentBatch.render()
    }
}
