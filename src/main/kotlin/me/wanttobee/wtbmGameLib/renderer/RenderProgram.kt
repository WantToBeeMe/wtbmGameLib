package me.wanttobee.wtbmGameLib.renderer

// we make sure we enter the batch system instead of the parameters of the batch system
// the reason for this is to make sure we can reuse the same batch system for multiple shaders that have the same structure
// there is no real reason to not do that anyway. It saves us from creating some renderBatches which is memory efficient
class RenderProgram(vertexPath : String?, fragmentPath : String?, batchSystem: BatchSystem?) {
    companion object{
        val DEFAULT_BATCH_SYSTEM = BatchSystem(intArrayOf(3,4,2))
    }
    val shader : Shader
    val batchSystem : BatchSystem

    init{
        shader = Shader(vertexPath,fragmentPath)
        this.batchSystem = batchSystem ?: DEFAULT_BATCH_SYSTEM
    }

    fun setDefaultBatchSize(vertices: Int, elements: Int){
        batchSystem.setDefaultBatchSize(vertices, elements)
    }
}