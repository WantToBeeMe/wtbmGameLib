package me.wanttobee.wtbmGameLib.rendering

import me.wanttobee.wtbmGameLib.Logger
import org.lwjgl.opengl.GL45.*

class RenderConfig {
    private lateinit var attributePointers : IntArray

    private var textureEnabled = false
    var verticesCount = 1000
        private set
    var elementCount = 500
        private set
    private var locked = false

    fun getVertexSize(): Int{
        return attributePointers.sum()
    }

    fun getAttribCount(): Int{
        return attributePointers.size
    }

    // add the positions for the vertexArray
    // Things like    Position,  Color,  UV
    // that would then be addVertexPointers(3,4,2)
    fun setVertexPointers(vararg attribPointers : Int) : RenderConfig{
        if(!locked) attributePointers = attribPointers
        return this
    }

    // enables the batch to allow for textures
    // enable this if you want your shader to also allow for texture rendering
    fun setIsTextureBatch() : RenderConfig{
        if(!locked)textureEnabled = true
        return this
    }

    // the amount of vertices 1 batch can hold
    // its defaulted to 1000, but you can change it to any number you want
    // (vertices are all the points )
    fun setVerticesCount(amount : Int) : RenderConfig{
        if(!locked)verticesCount = amount
        return this
    }
    // the amount of elements 1 batch can hold
    // its defaulted to 500 triangles (1 element is 1 triangle which is 3 indices)
    fun setElementCount(amount : Int) : RenderConfig{
        if(!locked)elementCount = amount
        return this
    }

    // this does some magic and crates the ID's needed for rendering
    // it will return a triple(vaoID, vboID, eboID)
    fun createBatchIDs() : Triple<Int,Int,Int>{
        locked = true
        if (!::attributePointers.isInitialized) {
            Logger.logError("cant create a new batch, use setVertexPointers() to set the different attribute pointers and there size",)
            return Triple(-1,-1,-1)
        }

        //generate and bind a vertex array object
        val vaoID = glGenVertexArrays() // we create a new vertex array possibility
        glBindVertexArray(vaoID)  // we are saying "everything that happens after this line, do it to this vertex array

        //allocate space for the vertices
        val vboID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, ((attributePointers.sum() * verticesCount) * Float.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        //create and upload indices buffer
        val eboID = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArray, GL_STATIC_DRAW) //enable the commented line here and in the render method to enable teh dynamic triangles
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,((elementCount*3) * Int.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        val vertexSizeFloats = attributePointers.sum()*Float.SIZE_BYTES
        var positionEncountered = 0
        for( i in attributePointers.indices){
            glVertexAttribPointer(i, attributePointers[i], GL_FLOAT, false, vertexSizeFloats, (positionEncountered *Float.SIZE_BYTES).toLong())
            positionEncountered += attributePointers[i]
        }

        glBindVertexArray( 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        return Triple(vaoID, vboID, eboID)
    }
}