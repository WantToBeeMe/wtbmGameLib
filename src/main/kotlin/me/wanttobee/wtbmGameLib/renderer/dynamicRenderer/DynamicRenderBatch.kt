package me.wanttobee.wtbmGameLib.renderer.dynamicRenderer

import me.wanttobee.wtbmGameLib.Logger
import me.wanttobee.wtbmGameLib.renderer.Texture2D
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL45.*

// this is one of the batches that a render batch program contains
// this batch contains all the information for a lot of elements, which will all be thrown at the gpu at once, cool

//vertex count is each dot on the screen
//element count is each triangle on the screen (each triangle is a row of 3 on its own in the array.)
class DynamicRenderBatch (
    private val maxVertices : Int,
    private val maxElements : Int,
    vertexAttributes : IntArray,
    IDs : Triple<Int,Int,Int>, // Triple(vao, vbo, ebo)
    private val enableTextures : Boolean
)  {
    private val TEXTURE_SLOTS = GL11.glGetInteger(GL13.GL_MAX_TEXTURE_UNITS)
    private val vaoID : Int = IDs.first
    private val vboID : Int = IDs.second
    private val eboID : Int = IDs.third

    //private val vertexAttributes = intArrayOf(3,4,2) // 3=position  4=color  2=textureUV
    private var attributeCount : Int = vertexAttributes.size
    private var vertexSize : Int = vertexAttributes.sum()

    private val vertexArray : FloatArray = FloatArray(maxVertices * vertexSize)
    private val elementArray : IntArray = IntArray(maxElements * 3)
    private val textureArray : Array<Texture2D?> = Array(TEXTURE_SLOTS){null}
    private val textureSlots = IntArray(TEXTURE_SLOTS){ i->i}

    private var vertexIndex = 0
    private var elementIndex = 0
    private var textureIndex = 0

    fun isEmpty() : Boolean{
        return elementIndex == 0 && vertexIndex == 0
        // we don't have to check textureIndex == 0
        // even if there is a texture, it doesn't matter if there are no vertices
    }

    fun hasSpace(vertices : Int, elements: Int, textures : Int = 0) : Boolean{
        val vertexHasSpace = vertices <= maxVertices-vertexIndex
        val elementHasSpace = elements <= maxElements-elementIndex
        val textureHasSpace = textures <= TEXTURE_SLOTS-textureIndex
                                                    //don't allow textures,or otherwise have enough space for the textures
        return elementHasSpace && vertexHasSpace && ((enableTextures && textureHasSpace) || !enableTextures)
    }

    fun getVerticesAvailable() : Int{
        return maxVertices - vertexIndex
    }
    fun getElementsAvailable() : Int{
        return maxElements - elementIndex
    }
    fun getTexturesAvailable() : Int{
        return TEXTURE_SLOTS - textureIndex
    }

    fun addTexture(texture: Texture2D) : Int{
        val index = textureArray.indexOf(texture)
        if(index != -1 && index < textureIndex) return index
        textureArray[textureIndex] = texture
        return textureIndex++
    }

    fun addVertex(vertex: FloatArray, textureID : Int = -1) : Int{
        val givenSize = vertex.size + (if(enableTextures) 1 else 0)
        // if textures are enabled, the vertex may be 1 less, which will be added later which is the texture ID
        if(givenSize != vertexSize){
            Logger.logError("vertex is not the same size as the initialized vertex size")
            return -1
        }
        for(i in vertex.indices){
            vertexArray[vertexIndex*vertexSize + i] = vertex[i]
        }
        if(enableTextures)
            vertexArray[vertexIndex*vertexSize + vertex.size] = textureID.toFloat()

        return vertexIndex++
    }

    fun addTriangle(first: Int, second:Int, third: Int) : Int{
        elementArray[elementIndex*3 + 0] = first
        elementArray[elementIndex*3 + 1] = second
        elementArray[elementIndex*3 + 2] = third
        return elementIndex++
    }

    fun clear(){
        vertexIndex = 0
        elementIndex = 0
        textureIndex = 0
        // this essentially means we reset the whole array. But resetting the whole array would be really inefficient.
        // instead we only reset our writing head (which are these indexes). then we tell the GPU to only draw the elements in the array until the writing head
        // so even though all the old data stays in the array, they won't be drawn until it is being overwritten by new data.
    }

    private fun generateInformativeElementString(newLines: Boolean) : String{
        var baseString = "["
        var elementBitIndex = 1
        for(e in elementArray){
            if(newLines && elementBitIndex % 3 == 1) baseString += "\n  ${elementBitIndex / 3}: "
            baseString += if(elementBitIndex % 3 == 0) "$e|" else "$e,"
            if(elementBitIndex / 3 >= elementIndex)
                return "$baseString..."
            elementBitIndex++
        }
        return baseString
    }
    private fun generateInformativeVerticesString(newLines: Boolean) : String{
        var baseString = "["
        var vertexBitIndex = 1
        for(v in vertexArray){
            if(newLines && vertexBitIndex % vertexSize == 1) baseString += "\n  ${vertexBitIndex / vertexSize}: "
            baseString += if(vertexBitIndex % vertexSize == 0) "$v|" else "$v,"
            if(vertexBitIndex / vertexSize >= vertexIndex)
                return "$baseString..."
            vertexBitIndex++
        }
        return baseString
    }

    fun printCurrentState(newLines :Boolean){
        Logger.logDebug("elements: $elementIndex")
        Logger.logDebug("elements: ${generateInformativeElementString(newLines)}")
        Logger.logDebug("vertices: $vertexIndex")
        Logger.logDebug("vertices: ${generateInformativeVerticesString(newLines)}")
        Logger.logDebug("textures: $textureIndex")
    }

    fun render(){
        //glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
        //uploading data or something

        for(i in 0 until textureIndex){
            glActiveTexture(GL_TEXTURE0 + i)
            glBindTexture(GL_TEXTURE_2D, textureArray[i]!!.id)
        // we can safely assert that it is not null,
        // it should never be null given this index, and if it is we want it to crash
        }
        DynamicRenderer.uploadIntArray("texturesSampler",textureSlots)

        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, elementArray)

        //Bind the VAO that we're using
        glBindVertexArray(vaoID)

        //Enable vertex attributes pointers
        for( i in 0 until attributeCount)
            glEnableVertexAttribArray(i)

        // we only draw the indices that have been set this iteration (elementIndex*3L)
        glDrawElements(GL_TRIANGLES, elementIndex*3, GL_UNSIGNED_INT, 0)

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
