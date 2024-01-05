package me.wanttobee.wtbmGameLib.renderer

import org.lwjgl.opengl.GL45.*
import kotlin.math.max


// each vertex has its own attributes like position, color and possibly a texture UV coordinates
// if you then want to create a vertex like above with these 3 different attributes the input would then be:
// 3,4,2   because: 3 = position(x,y,z)   4 = color(r,g,b,a)   2 = textureUV(u,v)
class BatchSystem(private val vertexAttributes: IntArray ) {
    private var defaultBatchSize: Pair<Int,Int> = Pair(1000,500)
    private var batchIndex = 0

    private val batches : MutableList<RenderBatch> = mutableListOf()


    fun reserveBatchSpot(vertices : Int, elements: Int) {
        for(i in batches.indices){
            if(batches[i].hasSpace(vertices,elements)){
                batchIndex = i
                return
            }
        }
        batchIndex = batches.size
        batches.add(createRenderBatch(vertices,elements))
    }
    fun addVertex(vertex: FloatArray) : Int{
        return batches[batchIndex]
            .addVertex(vertex)
    }
    fun addTriangle(first: Int, second: Int, third: Int) : Int {
        return batches[batchIndex]
            .addTriangle(first,second,third)
    }

    fun renderBatches(){
        for(b in batches){
            if(!b.isEmpty())
                b.render()
        }
        batchIndex = 0
    }


    fun setDefaultBatchSize(vertices: Int, elements: Int ){
        defaultBatchSize = Pair(vertices,elements)
    }

    // using this BatchConfig you will only be able to create fully dynamic render batches
    // this means that the vertexArray is dynamic like almost always, but also the element array is dynamic, which is less typical
    // this allows for more functionality but is a bit slower, you won't notice much of it, but it's good to know
    private fun createRenderBatch(vertices: Int, elements: Int) : RenderBatch {

        val batchSize = Pair(
            max(defaultBatchSize.first, vertices ),
            max(defaultBatchSize.second, elements ),
        )

        //generate and bind a vertex array object
        val vaoID = glGenVertexArrays() // we create a new vertex array possibility
        glBindVertexArray(vaoID)  // we are saying "everything that happens after this line, do it to this vertex array

        //allocate space for the vertices
        val vertexSize = vertexAttributes.sum()
        val vboID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, ((batchSize.first * vertexSize) * Float.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        //create and upload indices buffer
        val eboID = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArray, GL_STATIC_DRAW) //enable the commented line here and in the render method to enable teh dynamic triangles
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ((batchSize.second * 3) * Int.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        val vertexSizeFloats = vertexSize*Float.SIZE_BYTES
        var positionEncountered = 0
        for( i in vertexAttributes.indices){
            glVertexAttribPointer(i, vertexAttributes[i], GL_FLOAT, false, vertexSizeFloats, (positionEncountered *Float.SIZE_BYTES).toLong())
            positionEncountered += vertexAttributes[i]
        }

        glBindVertexArray( 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        return RenderBatch(batchSize.first, batchSize.second, vertexAttributes, Triple(vaoID,vboID, eboID))
    }

}