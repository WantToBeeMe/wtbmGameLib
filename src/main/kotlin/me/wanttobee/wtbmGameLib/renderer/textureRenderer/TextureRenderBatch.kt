package me.wanttobee.wtbmGameLib.renderer.textureRenderer

import me.wanttobee.wtbmGameLib.Logger
import me.wanttobee.wtbmGameLib.renderer.Texture2D
import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL45.*

// keep in mind, the max amount of textures 1 batch can hold doesn't mean that it will always be the max of that batch
// instead, this means that it can never hold more, but if all the texture spots are occupied,
// then the max is reached before set amount is reached
class TextureRenderBatch(
    private val maxTextures : Int, // the amount of textures this batches can hold
    vertexAttributes : IntArray,
    IDs : Triple<Int,Int,Int>
) {
    private val TEXTURE_SLOTS = GL11.glGetInteger(GL13.GL_MAX_TEXTURE_UNITS)
    private val vaoID : Int = IDs.first
    private val vboID : Int = IDs.second
    private val eboID : Int = IDs.third

    private var attributeCount : Int = vertexAttributes.size
    private var vertexSize : Int = vertexAttributes.sum()

    private val vertexArray : FloatArray = FloatArray(maxTextures * 4 * vertexSize) //for each texture there are 4 corners
    private val textureArray : Array<Texture2D?> = Array(TEXTURE_SLOTS){null}
    private val textureSlots = IntArray(TEXTURE_SLOTS){ i->i}

    private var quadIndex = 0 // the current index in the vertex/element array
    private var samplerIndex = 0 // the current index in the sampler array (which holds the textures)

    fun clear(){
        quadIndex = 0
        samplerIndex = 0
        // this essentially means we reset the whole array. But resetting the whole array would be really inefficient.
        // instead we only reset our writing head (which are these indexes). then we tell the GPU to only draw the elements in the array until the writing head
        // so even though all the old data stays in the array, they won't be drawn until it is being overwritten by new data.
    }

    fun isEmpty() : Boolean{
        return quadIndex == 0
        // we don't have to check the sampler Index, even if that is bigger than 0,
        // if quadIndex then is still zero, it's still empty
    }
    fun hasSpace(texture2D: Texture2D) : Boolean{
        if( quadIndex == maxTextures ) return false // the vertex array is full
        if( samplerIndex < TEXTURE_SLOTS) return true // the sampler still has space
        return textureArray.contains(texture2D) // if it does contain it, it means that for this texture, there is still space
        // this contains method will only be run if the sampler array is full already, so we don't have to check the index where it is contained
    }

    private fun textureSlot(texture2D: Texture2D) : Int{
        val currentIndex = textureArray.indexOf(texture2D)
        if(currentIndex != -1 && currentIndex < TEXTURE_SLOTS) return currentIndex
        textureArray[samplerIndex] = texture2D
        return samplerIndex++
    }

    private fun setVertex(vertexIndex : Int, x :Float, y: Float, z :Float, u : Float, v :Float, textureID : Int){
        val vi = vertexSize*vertexIndex
        vertexArray[vi + 0] = x
        vertexArray[vi + 1] = y
        vertexArray[vi + 2] = z

        vertexArray[vi + 3] = 1f
        vertexArray[vi + 4] = 1f
        vertexArray[vi + 5] = 1f
        vertexArray[vi + 6] = 1f

        vertexArray[vi + 7] = u
        vertexArray[vi + 8] = v

        vertexArray[vi + 9] = textureID.toFloat()
    }


    fun addTextureDrawCall(texture2D: Texture2D, source: Vector4f, dest: Vector4f){
        val texId = textureSlot(texture2D)
        val vi = quadIndex*4
        setVertex(vi+ 0, dest.x + dest.z, dest.y + dest.w, 0f, source.x + source.z, source.y + source.w,  texId)
        setVertex(vi+ 1, dest.x, dest.y, 0f, source.x , source.y,  texId)
        setVertex(vi+ 2, dest.x, dest.y + dest.w, 0f, source.x, source.y + source.w,  texId)
        setVertex(vi+ 3, dest.x + dest.z, dest.y, 0f, source.x + source.z, source.y,  texId)
        quadIndex++
    }

    private fun generateInformativeVerticesString(newLines: Boolean) : String{
        var baseString = "["
        var vertexBitIndex = 1
        for(v in vertexArray){
            if(newLines && vertexBitIndex % vertexSize == 1) baseString += "\n  ${vertexBitIndex / vertexSize}: "
            baseString += if(vertexBitIndex % vertexSize == 0) "$v|" else "$v,"
            if(vertexBitIndex / vertexSize >= (quadIndex*4))
                return "$baseString..."
            vertexBitIndex++
        }
        return baseString
    }
    fun printCurrentState(newLines :Boolean){
        Logger.logDebug("quads: $quadIndex (which translates to ${quadIndex*4} vertices, and ${quadIndex*2} triangles/elements)")
        Logger.logDebug("textures: $samplerIndex")
        Logger.logDebug( "verticesArray: ${generateInformativeVerticesString(newLines)}")
    }

    fun render(){
        //glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
        //uploading data or something

        for(i in 0 until samplerIndex){
            glActiveTexture(GL_TEXTURE0 + i)
            glBindTexture(GL_TEXTURE_2D, textureArray[i]!!.id)
            // we can safely assert that it is not null,
            // it should never be null given this index, and if it is we want it to crash
        }
        TextureRenderer.uploadIntArray("texturesSampler",textureSlots)

        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        //glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, elementArray) // not needed, element array is static

        //Bind the VAO that we're using
        glBindVertexArray(vaoID)

        //Enable vertex attributes pointers
        for( i in 0 until attributeCount)
            glEnableVertexAttribArray(i)

        // we only draw the indices that have been set this iteration (each quad had 2 triangle, each 3 corners ( 2*3 = 6 ))
        glDrawElements(GL_TRIANGLES, quadIndex*6, GL_UNSIGNED_INT, 0)

        //disable them again, because they are drawn
        for( i in 0 until attributeCount)
            glDisableVertexAttribArray(i)

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        // we now reset the indexes
        clear()
    }
}