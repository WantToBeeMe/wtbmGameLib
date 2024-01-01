package me.wanttobee.wtbmGameLib.renderer


import me.wanttobee.wtbmGameLib.Logger
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL20.*

class Shader(private val vertexPath : String?, private val fragmentPath : String?) {
    var shaderProgramID : Int = -1
        private set
    private var vertexSourceCode : String = ""
    private var fragmentSourceCode : String = ""


    // make sure that instead of having the default shader be a seperate object. it instead should be a source that is saved somewhere
    // and if the value is null then it should use that source instead
    init{
        vertexSourceCode =  if(vertexPath == null)
            DEFAULT_VERTEX_SOURCE
        else getSourceCode(vertexPath)

        fragmentSourceCode = if(fragmentPath == null)
            DEFAULT_FRAGMENT_SOURCE
        else getSourceCode(fragmentPath)

        shaderProgramID = compile(vertexSourceCode, fragmentSourceCode)
    }

    fun cleanup() {
        if (shaderProgramID != -1) {
            glDeleteProgram(shaderProgramID)
            shaderProgramID = -1
        }
    }

    fun info() : String{
        val numAttributes = glGetProgrami(shaderProgramID, GL_ACTIVE_ATTRIBUTES)
        val numUniforms = glGetProgrami(shaderProgramID, GL_ACTIVE_UNIFORMS)

        val stringBuilder = StringBuilder()
        stringBuilder.append("Shader Program ID: $shaderProgramID - ")
        stringBuilder.append("Active Attributes: $numAttributes - ")
        stringBuilder.append("Active Uniforms: $numUniforms\n")

        // Query and append attribute names and locations
        for (i in 0 until numAttributes) {
            val buffer1 = BufferUtils.createIntBuffer(1)
            val buffer2 = BufferUtils.createIntBuffer(1)
            buffer1.clear()
            buffer2.clear()

            val name = glGetActiveAttrib(shaderProgramID, i, buffer1, buffer2)
            val location = glGetAttribLocation(shaderProgramID, name)
            stringBuilder.append("A: $name located at location $location\n")
        }

        // Query and append uniform names and locations
        for (i in 0 until numUniforms) {
            val buffer1 = BufferUtils.createIntBuffer(1)
            val buffer2 = BufferUtils.createIntBuffer(1)
            buffer1.clear()
            buffer2.clear()

            val name = glGetActiveUniform(shaderProgramID, i, buffer1, buffer2)
            val location = glGetUniformLocation(shaderProgramID, name)
            stringBuilder.append("U: $name located at location $location\n")
        }

        val vertexLines = vertexSourceCode.split("\n".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        val fragmentLines = fragmentSourceCode.split("\n".toRegex())
            .dropLastWhile { it.isEmpty() }.toTypedArray()
        val grayCodeStart = "\u001B[90m"
        val resetFormatting = "\u001B[0m"
        val codeLineIndicator = ">   "

        stringBuilder.append("source code vertex: \n")
        for (line in vertexLines)
            stringBuilder.append(grayCodeStart).append(codeLineIndicator).append(line).append(resetFormatting).append("\n")
        stringBuilder.append("source code fragment: \n")
        for (line in fragmentLines)
            stringBuilder.append(grayCodeStart).append(codeLineIndicator).append(line).append(resetFormatting).append("\n")

        return stringBuilder.toString()
    }
    override fun toString(): String {
        val numAttributes = glGetProgrami(shaderProgramID, GL_ACTIVE_ATTRIBUTES)
        val numUniforms = glGetProgrami(shaderProgramID, GL_ACTIVE_UNIFORMS)

        val stringBuilder = StringBuilder()
        stringBuilder.append("<#Shader Program ID: $shaderProgramID, ")
        stringBuilder.append("Active Attributes: $numAttributes, ")
        stringBuilder.append("Active Uniforms: $numUniforms>")
        return stringBuilder.toString()
    }

    companion object{
        private val DEFAULT_VERTEX_SOURCE = getSourceCode("/shaders/default.vert")
        private val DEFAULT_FRAGMENT_SOURCE = getSourceCode("/shaders/default.frag")

        private fun getSourceCode(path: String) : String{
            val fragmentStream = this::class.java.getResourceAsStream(path)
            return fragmentStream?.bufferedReader()?.use { it.readText() } ?:
            run {
                Logger.logError("wrong path $path");
                assert(false); ""
            }
        }

        //only once after creating the shader
        //makes sure the shader is loaded in openGL
        private fun compile(vertexSource : String, fragmentSource:String) : Int{
            var shaderProgramID = -1
            //compile and link the shaders
            //load and compile the vertex shaders
            val vertexID : Int = glCreateShader(GL_VERTEX_SHADER)
            //pass the shader to the gpu
            glShaderSource(vertexID, vertexSource)
            glCompileShader(vertexID)
            //check for errors in compilation presses
            val successVertex = glGetShaderi(vertexID, GL_COMPILE_STATUS)
            if(successVertex == GL_FALSE){
                val len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH)
                Logger.logError(" vertex shader compilation failed \n ${glGetShaderInfoLog(vertexID, len)}")
                glDeleteShader(vertexID)//taking the shader out of memory again (it will assert false so it's not really needed, but it's not bad to do it anyway)
                assert(false);
                return -1
            }

            //load and compile the vertex shaders
            val fragmentID : Int = glCreateShader(GL_FRAGMENT_SHADER)
            //pass the shader to the gpu
            glShaderSource(fragmentID, fragmentSource)
            glCompileShader(fragmentID)
            //check for errors in compilation presses
            val successFragment = glGetShaderi(fragmentID, GL_COMPILE_STATUS)
            if(successFragment == GL_FALSE){
                val len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH)
                Logger.logError("fragment shader compilation failed \n ${glGetShaderInfoLog(fragmentID, len)}")
                glDeleteShader(fragmentID)//taking the shader out of memory again (it will assert false so it's not really needed, but it's not bad to do it anyway)
                assert(false);
                return -1
            }

            //link shaders and check for errors
            shaderProgramID = glCreateProgram()
            //attach the 2 shaders to this program
            glAttachShader(shaderProgramID, vertexID)
            glAttachShader(shaderProgramID, fragmentID)
            //try to link the program again
            glLinkProgram(shaderProgramID)
            val successLink = glGetProgrami(shaderProgramID, GL_LINK_STATUS)
            if(successLink == GL_FALSE){
                val len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH)
                Logger.logError("linking shader failed \n ${glGetProgramInfoLog(fragmentID, len)}")
                glDeleteProgram(shaderProgramID)
                glDeleteShader(vertexID)
                glDeleteShader(fragmentID)
                assert(false);
                return -1
            }

            //the program has successfully compiled which means we don't really need the shaders anymore.
            //therefore we can just detach and remove the shaders to free up some memory
            glDetachShader(shaderProgramID, vertexID)
            glDetachShader(shaderProgramID, fragmentID)
            glDeleteShader(vertexID)
            glDeleteShader(fragmentID)

            return shaderProgramID
        }
    }


}
