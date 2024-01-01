package me.wanttobee.wtbmGameLib.rendering

class RenderProgram(private val vertexPath : String?, private val fragmentPath : String?, renderConfig : RenderConfig? = null) {
    val shader : Shader = Shader(vertexPath, fragmentPath)
    val shaderProgramID = shader.shaderProgramID
    private val batches : MutableList<RenderBatch> = mutableListOf()
    private val renderConfig : RenderConfig

    init{
        this.renderConfig = renderConfig ?: RenderConfig()
            .setIsTextureBatch().setVertexPointers(3,4,2)
        batches.add(RenderBatch(this.renderConfig))
    }

    override fun toString(): String {
        return "<#RenderProgram ${batches.size} batch(es),  ${shader}>"
    }
    fun info() : String{
        var returnString = "RenderProgram has ${batches.size} batch(es):"
        for(b in batches){
            returnString += "\n"
            returnString += b.info()
        }
        return returnString
    }

    fun render(){
        for(batch in batches)
            batch.render()
    }

    fun getBatchForAction(providedVertices: Int, providedElements: Int) : RenderBatch{
        for(b in batches){
            if(b.checkIfEnoughPlace(providedVertices, providedElements))
                return b
        }
        val newBatch = RenderBatch(renderConfig)
        batches.add(newBatch)
        return newBatch
    }

    fun addVertex(vertexAttributes : FloatArray) : Int{
        val batch = getBatchForAction(1,0)
        return batch.addVertex(vertexAttributes)
    }

    //TODO: THIS IS REALLY BAD!!!
    //  you cant just make the addVertex and addTriangle method separate
    //  you dont know if they will combine in to the same batch or if for example one batch only has place for elements and not for vertexes
    fun addTriangle(first:Int, second: Int, third:Int) : Int{
        val batch = getBatchForAction(0,1)
        return batch.addTriangle(first, second, third)
    }
}