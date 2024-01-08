package me.wanttobee.wtbmGameLib.renderer

import me.wanttobee.wtbmGameLib.Logger
import org.joml.*
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45.*


// TODO: make more render system types
//   StaticRenderer = a renderer which runs on dirty flags, and objects subscribed to it
//   VoxelRenderer = a renderer which has a perfectly predefined vertex/element array, and only makes use of uniforms to tell how the elements should be colored

// This is the interface that all the renderers will have. This has multiple benefits
// this makes sure each renderer can make use of shaders, and also that each render can upload uniforms
// instead of having to create this functionality, we just create a renderer that already has this build in. cool
interface IRenderer<RP : IRenderProgram> {
    var currentShader : Shader
    var currentBatch : RP

    fun startDrawing()
    fun endDrawing()

    // change the shader without changing the current batch (null if change back to default)
    fun changeProgram(shaderProgram: Shader?)

    // changing the shader and the batch. set second parameter to desired batch,
    // or to null if you want the batch to go back to default (stays unchanged if it was already default)
    fun changeProgram(shaderProgram: Shader?, batchProgram: RP?)


    var blendOn : Boolean
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


    var depthTestOn: Boolean
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

    fun printCurrentState(newLines: Boolean){
        currentBatch.printCurrentState(newLines)
    }
}