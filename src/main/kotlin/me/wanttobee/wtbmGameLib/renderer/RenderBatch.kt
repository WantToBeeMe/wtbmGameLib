package me.wanttobee.wtbmGameLib.renderer

import me.wanttobee.wtbmGameLib.Logger
import org.lwjgl.opengl.GL45.*

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

    private var vertexArray : FloatArray
    private var elementArray : IntArray

    private var elementIndex = 0
    private var vertexIndex = 0

    init {
        vertexArray = FloatArray(maxVertices * vertexSize)
        elementArray = IntArray(maxElements * 3)
    }

    fun isEmpty() : Boolean{
        return elementIndex == 0 && vertexIndex == 0
    }

    fun hasSpace(vertices : Int, elements: Int) : Boolean{
        val vertexHasSpace = vertices <= maxVertices-vertexIndex
        val elementHasSpace = elements <= maxElements-elementIndex
        return elementHasSpace && vertexHasSpace
    }

    fun getVerticesAvailable() : Int{
        return maxVertices - vertexIndex
    }
    fun getElementsAvailable() : Int{
        return maxElements - elementIndex
    }

    fun checkIfEnoughPlace(providedVertices: Int, providedElements: Int) : Boolean{
        return getElementsAvailable() >= providedElements && getVerticesAvailable() >= providedVertices
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