package me.wanttobee.wtbmGameLib.rendering

import org.joml.Matrix4f
import org.lwjgl.opengl.GL45.*

object RenderTest {
    private val posSize = 3
    private val colorSize = 4
    private val textureCordSize = 2
    private var _generated = false
    //-1 is left, 1 is right side of screen
    private var vertexArray : FloatArray = floatArrayOf(
        //position              //color                    //Tex Cords
        0.4f, -0.5f ,0.0f,      1.0f, 0.0f, 0.0f, 1.0f,    0.0f,0.0f, //R-0 - bottom right
        -0.5f, 0.5f, 0.0f,      0.0f, 1.0f, 0.0f, 1.0f,    0.0f,0.0f, //G-1 - top left
        0.5f, 0.5f, 0.0f,       0.0f, 0.0f, 1.0f, 1.0f,    0.0f,0.0f, //B-2 - top right
        -0.5f, -0.5f, 0.0f,     0.0f, 1.0f, 1.0f, 1.0f,    0.0f,0.0f, //C-3-  bottom left

        -0.0f, -0.7f, 0.0f,     1.0f, 1.0f, 1.0f, 1.0f,    0.0f,0.0f,  //W-4-  bottom center
    )

    // IMPORTANT??? : This must be in counter - clockwise order? do some more research before defining the real thing of this
    private var elementArray : IntArray = intArrayOf(
        2,1,0, // top right triangle
        0,1,3, // bottom left triangle
        0,3,4
    )

    private var vaoID : Int = 0
    private var vboID : Int = 0
    private var eboID : Int = 0

    fun generateRenderData(){
        if(_generated) return
        _generated = true
        //generate and bind a vertex array object
        vaoID = glGenVertexArrays() // we create a new vertex array possibility
        glBindVertexArray(vaoID)  // we are saying "everything that happens after this line, do it to this vertex array


        //allocate space for the vertices
        vboID = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferData(GL_ARRAY_BUFFER, (vertexArray.size * Float.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        //create and upload indices buffer
        eboID = glGenBuffers()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementArray, GL_STATIC_DRAW) //enable the commented line here and in the render method to enable teh dynamic triangles
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER,(triangles.size * Int.SIZE_BYTES).toLong(), GL_DYNAMIC_DRAW)

        val vertexSizeFloats = (posSize + colorSize + textureCordSize)*Float.SIZE_BYTES
        glVertexAttribPointer(0, posSize, GL_FLOAT, false, vertexSizeFloats, 0.toLong())
        glVertexAttribPointer(1, colorSize,GL_FLOAT, false, vertexSizeFloats, (posSize *Float.SIZE_BYTES).toLong())
        glVertexAttribPointer(2, textureCordSize,GL_FLOAT, false, vertexSizeFloats, ((posSize + colorSize)*Float.SIZE_BYTES).toLong())

        glBindVertexArray( 0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    fun clear() {
        vertexArray = FloatArray(0)
        elementArray = IntArray(0)

        if (!_generated) return
        glDeleteVertexArrays(vaoID)
        glDeleteBuffers(vboID)
        glDeleteBuffers(eboID)
        vaoID = 0
        vboID = 0
        eboID = 0
        _generated = false
    }

    private val identity : Matrix4f = Matrix4f().identity()
    fun render(){
        //ShaderManager.uploadMat4f("ProjMtx", identity)

        glPolygonMode( GL_FRONT_AND_BACK, GL_FILL );
        //uploading data or something
        glBindBuffer(GL_ARRAY_BUFFER, vboID)
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexArray)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID)
        //glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, triangles)

        //Bind the VAO that we're using
        glBindVertexArray(vaoID)

        //Enable vertex attributes pointers
        glEnableVertexAttribArray(0)
        glEnableVertexAttribArray(1)
        glEnableVertexAttribArray(2)
        glDrawElements(GL_TRIANGLES, elementArray.size, GL_UNSIGNED_INT, 0)

        //disable them again, because they are drawn
        glDisableVertexAttribArray(0)
        glDisableVertexAttribArray(1)
        glDisableVertexAttribArray(2)

        glBindVertexArray(0)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}
