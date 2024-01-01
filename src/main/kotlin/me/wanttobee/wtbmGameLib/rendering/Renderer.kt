package me.wanttobee.wtbmGameLib.rendering

import me.wanttobee.wtbmGameLib.Logger

import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20.*

object Renderer {
    val DEFAULT_PROGRAM : RenderProgram = RenderProgram(null,null, null)
    var currentProgram : RenderProgram = DEFAULT_PROGRAM
        private set

    fun reset(){
        toggleBlend(false)
        toggleDepthTest(false)
    }

    fun toggleBlend(on : Boolean){
        if(on){
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        }else{
            glDisable(GL_BLEND)
        }
    }
    fun toggleDepthTest(on : Boolean){
        if(on){
            glEnable(GL_DEPTH_TEST)
            glDepthFunc(GL_LESS)
        } else{
            glDisable(GL_DEPTH_TEST)
        }
    }

    fun useProgram(program: RenderProgram){
        currentProgram = program
    }
    fun endProgram(){
        currentProgram.render()
        currentProgram = DEFAULT_PROGRAM
    }


    private fun getVarLocation(uniformName: String) : Int{
        val varLocation = glGetUniformLocation(currentProgram.shaderProgramID , uniformName)
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
    fun uploadTexture(uniformName : String, slot : Int){
        //the same as uploading an int, but I know I will forget so that's why I have this func
        uploadInt(uniformName, slot)
    }
    fun uploadIntArray(uniformName : String, array : IntArray ){
        val varLocation = getVarLocation(uniformName)
        if(varLocation < 0) return
        glUniform1iv(varLocation, array)
        //v means Value
        //that means its a value pointer,
    }
}