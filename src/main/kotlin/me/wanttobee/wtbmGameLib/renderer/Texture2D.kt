package me.wanttobee.wtbmGameLib.renderer

import me.wanttobee.wtbmGameLib.Logger
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBImage.stbi_image_free
import org.lwjgl.stb.STBImage.stbi_load
import java.io.File

// hold all the information for the textures
//TODO: make it so you can decide how you want to to wrap, and how you want it to scale
class Texture2D(val filepath: String, pathFromResources : Boolean,resizeFilter : Int = RESIZE_LINEAR, wrapFilter :Int = WRAP_CLAMP ) {
    var id: Int = -1
        private set

    var width: Int = 0
        private set
    var height: Int = 0
        private set

    companion object {
        const val WRAP_REPEAT = GL_REPEAT
        const val WRAP_CLAMP = GL_CLAMP
        const val RESIZE_NEAREST = GL_NEAREST
        const val RESIZE_LINEAR = GL_LINEAR
    }

    init {
        compile(pathFromResources, wrapFilter, resizeFilter)
    }

    private fun compile(pathFromResources: Boolean, wrapFilter: Int, resizeFilter: Int) {
        // generate texture on GPU
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id)

        // Set texture parameterless
        // Repeat image in both directions
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapFilter) // left and right
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapFilter) // up and down

        // When what to do when minimizing or magnifying //nearest is pixelated
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, resizeFilter) // minimizing
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, resizeFilter) // magnifying

        val widthBuffer = BufferUtils.createIntBuffer(1)
        val heightBuffer = BufferUtils.createIntBuffer(1)
        val channels = BufferUtils.createIntBuffer(1)

        var path = filepath
        if (pathFromResources) {
            val inputStream = this.javaClass.getResourceAsStream(filepath)
            if (inputStream == null)
                Logger.logError("Could not find image in resources directory: $filepath", true)
            val tempFile = File.createTempFile("tempTextureFile", ".png")
            tempFile.deleteOnExit()
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            path = File(tempFile.toURI()).absolutePath
        }
        // desired channels 0 means that it will just give you the channels that it has
        val image = stbi_load(path, widthBuffer, heightBuffer, channels, 4) ?: run {
            Logger.logError("Could not load the image from: $path", true)
            return
        }
        width = widthBuffer.get(0)
        height = heightBuffer.get(0)

        glTexImage2D(
            GL_TEXTURE_2D, 0,   // something for mipmap levels, 0 is the default, IDK exactly
            GL_RGBA, width, height, 0, // we don't really care about the border, IDK lol
            GL_RGBA, GL_UNSIGNED_BYTE, image
        )

        Logger.logInfo(
            "loaded image: $id, it is ${width}px by ${height}px and with ${channels.get(0)} channels from path: ($filepath)",
            true
        )

        glBindTexture(GL_TEXTURE_2D, 0)
        stbi_image_free(image)
        // this frees the memory that the stbi has allocated
        // we no longer need this data there anyway, we only need the id now
    }
}
