package me.wanttobee.wtbmGameLib.renderer

import me.wanttobee.wtbmGameLib.Logger
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45.*

object Renderer {
    private val DEFAULT_RENDER_PROGRAM = RenderProgram(null,null,null)
    private var currentRenderProgram = DEFAULT_RENDER_PROGRAM

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
        glUseProgram(currentRenderProgram.shader.shaderProgramID)
    }

    fun changeRenderProgram(program: RenderProgram){
        render()
        currentRenderProgram = program
        glUseProgram(currentRenderProgram.shader.shaderProgramID)
    }

    // alies for endDrawing, but it sounds better if you want to go back to the default Render program
    fun stopCurrentRenderProgram(){
        endDrawing()
    }

    fun endDrawing(){
        render()
        if(currentRenderProgram != DEFAULT_RENDER_PROGRAM){
            currentRenderProgram = DEFAULT_RENDER_PROGRAM
            glUseProgram(currentRenderProgram.shader.shaderProgramID)
        }
    }

    fun reserveBatchSpot(vertices : Int, elements: Int) {
        currentRenderProgram.batchSystem.reserveBatchSpot(vertices,elements)
    }
    fun addVertex(vertex: FloatArray) : Int{
        return currentRenderProgram.batchSystem.addVertex(vertex)
    }
    fun addTriangle(first: Int, second: Int, third: Int) : Int {
        return currentRenderProgram.batchSystem.addTriangle(first,second,third)
    }

    private val identity : Matrix4f = Matrix4f().identity()
    fun render(){
        uploadMat4f("ProjMtx", identity)

        currentRenderProgram.batchSystem.renderBatches()
    }



    private fun getVarLocation(uniformName: String) : Int{
        val varLocation = glGetUniformLocation(currentRenderProgram.shader.shaderProgramID , uniformName)
        if(varLocation < 0)
            Logger.logWarning("Cant find: `${uniformName}` Uniform in the current shader", true)
        return varLocation
    }
    //uploading uniforms to the shader, speaks for itself I think
    fun uploadMat4f(uniformName : String, mat4 : Matrix4f){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        val matBuffer = BufferUtils.createFloatBuffer(16)
        mat4.get(matBuffer)
        glUniformMatrix4fv(varLocation, false, matBuffer)
    }
    fun uploadMat3f(uniformName : String, mat3 : Matrix3f){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        val matBuffer = BufferUtils.createFloatBuffer(9)
        mat3.get(matBuffer)
        glUniformMatrix3fv(varLocation, false, matBuffer)
    }
    fun uploadVec4f(uniformName : String, vec4 : Vector4f){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform4f(varLocation, vec4.x, vec4.y, vec4.z, vec4.w)
    }
    fun uploadVec3f(uniformName : String, vec3 : Vector3f){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform3f(varLocation, vec3.x, vec3.y, vec3.z)
    }
    fun uploadVec2f(uniformName : String, vec2 : Vector2f){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform2f(varLocation, vec2.x, vec2.y)
    }
    fun uploadFloat(uniformName: String, fl : Float){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform1f(varLocation, fl)
    }
    fun uploadInt(uniformName : String, intt : Int){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform1i(varLocation, intt)
    }
    fun uploadTexture(uniformName : String, slot : Int, texture: Texture2D){
        // when uploading more textures at once, it just like uploading an intArray
        if(slot < 0 || slot > 31){
            Logger.logError("cant upload texture, slot: $slot is out of range (0..31)")
            return
        }
        uploadInt(uniformName, slot)   //the same as uploading an int
        glActiveTexture(GL_TEXTURE0 + slot)
        glBindTexture(GL_TEXTURE_2D, texture.id)
    }

    fun uploadIntArray(uniformName : String, array : IntArray ){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform1iv(varLocation, array)
        //v means Value
        //that means its a value pointer,
    }
}
