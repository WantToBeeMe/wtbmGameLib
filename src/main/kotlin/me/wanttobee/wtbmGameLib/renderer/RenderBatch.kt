package me.wanttobee.wtbmGameLib.renderer

import me.wanttobee.wtbmGameLib.Logger
import org.lwjgl.opengl.GL45.*

// TODO: Dirty flags
//   so in order to save performance, we might consider adding dirty flagging inside the bathes
//   If we are considering this, this means that we dont recreate the array every time, but instead only recreate when its flagged with being dirty
//   Or even better, only re assign the bit that changed (but that would require separate objects which save the buffer and there location of the vertices)
//   However, This is super cool to add, its not to replace this batch, it will be a different type of batch
//   (a reason for this could be 3 different big 3D models, these shouldn't have to be re-added to the array every time,
//   instead we add them once, the 3 different big models then know that there index goes from index 12 to 340, and if it needs to change, we just let the model deal with that)

//vertex count is each dot on the screen
//element count is each triangle on the screen (each triangle is a row of 3 on its own in the array.)
class RenderBatch(
    private val maxVertices : Int,
    private val maxElements : Int,
    vertexAttributes : IntArray,
    IDs : Triple<Int,Int,Int>, // Triple(vao, vbo, ebo)
) {
    private var vaoID : Int = IDs.first
    private var vboID : Int = IDs.second
    private var eboID : Int = IDs.third

    //private val vertexAttributes = intArrayOf(3,4,2) // 3=position  4=color  2=textureUV
    private var attribCount : Int = vertexAttributes.size
    private var vertexSize : Int = vertexAttributes.sum()

    private val vertexArray : FloatArray = FloatArray(maxVertices * vertexSize)
    private val elementArray : IntArray = IntArray(maxElements * 3)
    private val textureArray : Array<Texture2D?> = Array(32){null}

    private var vertexIndex = 0
    private var elementIndex = 0
    private var textureIndex = 0

    fun isEmpty() : Boolean{
        return elementIndex == 0 && vertexIndex == 0
        // we don't have to check textureIndex == 0
        // even if there is a texture, it doesn't matter if there are no vertices
    }

    fun hasSpace(vertices : Int, elements: Int) : Boolean{
        val vertexHasSpace = vertices <= maxVertices-vertexIndex
        val elementHasSpace = elements <= maxElements-elementIndex
        //val textureHasSpace = textures  <= 32-textureIndex
        return elementHasSpace && vertexHasSpace // textureHasSpace
    }

    fun getVerticesAvailable() : Int{
        return maxVertices - vertexIndex
    }
    fun getElementsAvailable() : Int{
        return maxElements - elementIndex
    }
    fun getTexturesAvailable() : Int{
        return 32 - textureIndex
    }

    fun addVertex(vertex: FloatArray) : Int{
        if(vertex.size != vertexSize){
            Logger.logError("vertex is not the same size as the initialized vertex size")
            return -1
        }
        for(i in vertex.indices){
            vertexArray[vertexIndex*vertexSize + i] = vertex[i]
        }
        return vertexIndex++
    }

    fun addTriangle(first: Int, second:Int, third: Int) : Int{
        elementArray[elementIndex*3 + 0] = first
        elementArray[elementIndex*3 + 1] = second
        elementArray[elementIndex*3 + 2] = third
        return elementIndex++
    }

    fun render(){

        //glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
        //uploading data or something
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, elementArray)

        //Bind the VAO that we're using
        glBindVertexArray(vaoID)

        //Enable vertex attributes pointers
        for( i in 0 until attribCount)
            glEnableVertexAttribArray(i)

        // we only draw the indices that have been set this iteration (elementIndex*3L)
        glDrawElements(GL_TRIANGLES, elementIndex*3, GL_UNSIGNED_INT, 0)

        //disable them again, because they are drawn
        for( i in 0 until attribCount)
            glDisableVertexAttribArray(i)

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        // we now reset the indexes
        vertexIndex = 0
        elementIndex = 0
        // this essentially means we reset the whole array. But resetting the whole array would be really inefficient.
        // instead we only reset our writing head (which are these indexes). then we tell the GPU to only draw the elements in the array until the writing head
        // so even though all the old data stays in the array, they won't be drawn until it is being overwritten by new data.
    }
}