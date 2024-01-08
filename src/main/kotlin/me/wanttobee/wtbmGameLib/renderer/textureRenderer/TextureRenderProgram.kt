package me.wanttobee.wtbmGameLib.renderer.textureRenderer

import me.wanttobee.wtbmGameLib.Logger
import me.wanttobee.wtbmGameLib.renderer.IRenderProgram
import me.wanttobee.wtbmGameLib.renderer.Texture2D
import org.joml.Vector4f
import org.lwjgl.opengl.GL45.*

class TextureRenderProgram(private val vertexAttributes: IntArray, private val maxTextures : Int) : IRenderProgram {
    private val elementArray : IntArray = IntArray(maxTextures * 6)
    // for each texture there are 2 triangle required, (each triangle has 3  corners)
    private val batches : MutableList<TextureRenderBatch> = mutableListOf()


    init{
        for(i in 0 until maxTextures){
            val i6 = i*6
            elementArray[i6 + 0] = 2 //
            elementArray[i6 + 1] = 1 //   Top right triangle
            elementArray[i6 + 2] = 0 //

            elementArray[i6 + 3] = 0 //
            elementArray[i6 + 4] = 1 //   Bottom Left triangle
            elementArray[i6 + 5] = 3 //
        }
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

    override fun printCurrentState(newLines: Boolean) {
        for(b in batches.indices){
            Logger.logDebug("batch $b:")
            batches[b].printCurrentState(newLines)
        }
    }

    fun drawTexture(texture2D: Texture2D, source: Vector4f, dest: Vector4f){
        for(b in batches){
            if(b.hasSpace(texture2D)){
                b.addTextureDrawCall(texture2D,source,dest)
                return
            }
        }

        val newBatch = createRenderBatch()
        newBatch.addTextureDrawCall(texture2D,source,dest)
        batches.add(newBatch)
    }

    private fun createRenderBatch() : TextureRenderBatch {
        //generate and bind a vertex array object
        val vaoID = glGenVertexArrays() // we create a new vertex array possibility
        glBindVertexArray(vaoID)  // we are saying "everything that happens after this line, do it to this vertex array

        //allocate space for the vertices
        val vertexSize = vertexAttributes.sum()
        val vboID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, (((maxTextures * 4) * vertexSize) * Float.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        //create and upload indices buffer
        val eboID = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArray, GL_STATIC_DRAW) //enable the commented line here and in the render method to enable teh dynamic triangles
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, ((maxTextures * 6) * Int.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        val vertexSizeFloats = vertexSize*Float.SIZE_BYTES
        var positionEncountered = 0
        for( i in vertexAttributes.indices){
            glVertexAttribPointer(i, vertexAttributes[i], GL_FLOAT, false, vertexSizeFloats, (positionEncountered *Float.SIZE_BYTES).toLong())
            positionEncountered += vertexAttributes[i]
        }

        glBindVertexArray( 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        return TextureRenderBatch(maxTextures,  vertexAttributes, Triple(vaoID,vboID, eboID))
    }
}