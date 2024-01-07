package me.wanttobee.wtbmGameLib.renderer

import me.wanttobee.wtbmGameLib.Logger
import me.wanttobee.wtbmGameLib.renderer.dynamicRendererr.RenderBatchProgram
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45.*

// This is the interface that all the renderers will have. This has multiple benefits
// this makes sure each renderer can make use of shaders, and also that each render can upload uniforms
// instead of having to create this functionality, we just create a renderer that already has this build in. cool
interface IRenderer {
    var currentShader : Shader
    var currentBatch : RenderBatchProgram

    private fun getVarLocation(uniformName: String) : Int{
        val varLocation = glGetUniformLocation(currentShader.shaderProgramID , uniformName)
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