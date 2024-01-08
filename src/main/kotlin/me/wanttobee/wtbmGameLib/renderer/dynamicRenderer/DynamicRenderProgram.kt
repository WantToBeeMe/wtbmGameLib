package me.wanttobee.wtbmGameLib.renderer.dynamicRenderer

import me.wanttobee.wtbmGameLib.Logger
import me.wanttobee.wtbmGameLib.renderer.IRenderProgram
import me.wanttobee.wtbmGameLib.renderer.Texture2D
import org.lwjgl.opengl.GL45.*
import kotlin.math.max

// The RenderBatchProgram is responsible for handling all the different RenderBatches
// By creating a new program you essentially create a new list of batches, but this class will provide you with a lot of helpful stuff
// you can enable texture be setting the second parameter of this class to true
//  this will only have effect on addingVertexes and addingTextures. if you have set it to false, addingTextures won't do anything obviously\
//  addVertex want you to add an array that has the same vertex size as you specified in the beginning of this program
//  if this is not the case it will log an error. However, if again that texture boolean is set to true, this means that you're
//  the array should instead be 1 slot smaller than you specified, then you can use the second parameter to provide an int for
//  the value you got back when you used the method addTexture
class DynamicRenderProgram(private val vertexAttributes: IntArray, private val willContainTextures : Boolean) : IRenderProgram{
    private var defaultMaxVertices = 1000
    private var defaultMaxElements = 500
    private val batches : MutableList<DynamicRenderBatch> = mutableListOf()
    private var batchIndex = 0

    fun setDefaultBatchSize(vertices: Int, elements: Int){
        defaultMaxVertices = vertices
        defaultMaxElements = elements
    }

    override fun clear(){
        for(b in batches){
            b.clear()
        }
    }
    override fun render(){
        for(b in batches){
            if(!b.isEmpty())
                b.render()
        }
    }

    fun reserveBatchSpot(vertices : Int, elements: Int, textures : Int = 0) {
        for(i in batches.indices){
            if(batches[i].hasSpace(vertices,elements,textures)){
                batchIndex = i
                return
            }
        }
        batchIndex = batches.size
        batches.add(createRenderBatch(vertices,elements))
    }
    fun addTexture(texture2D: Texture2D) : Int{
        return batches[batchIndex]
            .addTexture(texture2D)
    }
    fun addVertex(vertex: FloatArray, textureID : Int = -1) : Int{
        return batches[batchIndex]
            .addVertex(vertex, textureID)
    }
    fun addTriangle(first: Int, second: Int, third: Int) : Int {
        return batches[batchIndex]
            .addTriangle(first,second,third)
    }

    override fun printCurrentState(newLines: Boolean){
        for(b in batches.indices){
            Logger.logDebug("batch $b:")
            batches[b].printCurrentState(newLines)
        }
    }

    private fun createRenderBatch(vertices: Int, elements: Int) : DynamicRenderBatch {
        val maxVertices = max(defaultMaxVertices, vertices )
        val maxElements = max(defaultMaxElements, elements )

        //generate and bind a vertex array object
        val vaoID = glGenVertexArrays() // we create a new vertex array possibility
        glBindVertexArray(vaoID)  // we are saying "everything that happens after this line, do it to this vertex array

        //allocate space for the vertices
        val vertexSize = vertexAttributes.sum()
        val vboID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, ((maxVertices * vertexSize) * Float.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        //create and upload indices buffer
        val eboID = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArray, GL_STATIC_DRAW) //enable the commented line here and in the render method to enable teh dynamic triangles
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ((maxElements * 3) * Int.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        val vertexSizeFloats = vertexSize*Float.SIZE_BYTES
        var positionEncountered = 0
        for( i in vertexAttributes.indices){
            glVertexAttribPointer(i, vertexAttributes[i], GL_FLOAT, false, vertexSizeFloats, (positionEncountered *Float.SIZE_BYTES).toLong())
            positionEncountered += vertexAttributes[i]
        }

        glBindVertexArray( 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        return DynamicRenderBatch(maxVertices, maxElements, vertexAttributes, Triple(vaoID,vboID, eboID), willContainTextures)
    }
}