package me.wanttobee.wtbmGameLib

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

// so this is some simple helper object which already implemented a functional language translator
// you can add your own translation by doing : `Translator.addTranslation("english", "path/within_resources_folder/to_lang_file.yml")`
// by default the native language is set to "english" so if you add a translation to that native you don't have
// to also set the native, it's done for you :P
// anyway, if you to change it, lets say to "dutch" just use setNativeLanguage("dutch") and every
// translation that is being done will be Dutch from here on out. if a translation doesn't exists it will still
// go back to "english" by default for that specific translation. this is the fallback language which you
// can also change by setFallbackLanguage(...)
// if no translation in that fallback language can be found then it will just set the given key 'items.sword' for example
//
// translations are saved in yaml files.
// first:
//   init: "A"
//   second: "B"
//   third: "C"
// myList:
//   - "D"
//   - "E"
// translate("first") -> "A"
// translate("first.second") -> "B"
// translate("first.forth") -> "first.forth" (it cant find it, so it will be the translation key)
// translate("myList.0") -> "D"
// translate("myList.1") -> "E"

object Translator {
    private var nativeLanguage : String = "english"
    private var nativeFallbackLanguage : String = "english"

    private val translations: MutableMap<String, Map<String, String>> = mutableMapOf()
    fun setNativeLanguage(newNative : String){
        val lowerNative = newNative.lowercase()
        nativeLanguage = lowerNative
        Logger.logInfo("native language has been changed to $lowerNative")
    }
    fun setFallbackLanguage(fallbackNative : String){
        val lowerNative = fallbackNative.lowercase()
        nativeFallbackLanguage = lowerNative
        Logger.logInfo("fallback native language has been changed to $lowerNative")
    }
    fun getNatives() : MutableSet<String>{
        return translations.keys
    }

    fun translate(text: String): String? {
        var translation = translations[nativeLanguage]?.get(text)
        if(translation == null){
            translation = translations[nativeFallbackLanguage]?.get(text)
            if(translation == null)
                Logger.logWarning("Translation in $nativeLanguage not found for key: $text", true)
            else
                Logger.logWarning("Translation in $nativeLanguage not found for key: $text, falling back to $nativeFallbackLanguage", true)
        }
        return translation
    }

    fun addTranslation(native: String, path: String): Boolean {
        val lowerNative = native.lowercase()
        try {
            val inputStream: InputStream? = Translator::class.java.classLoader.getResourceAsStream(path)
            return if (inputStream != null) {
                val yaml = Yaml()
                val translationMap = flattenYaml(yaml.load(inputStream), "")

                translations[lowerNative] = translationMap
                Logger.logInfo("Loaded the translations for $lowerNative")
                true
            } else {
                Logger.logError("Cannot find $lowerNative translation at $path")
                false
            }
        } catch (e: Exception) {
            Logger.logError("Error loading $lowerNative translation at $path")
            e.printStackTrace()
            return false
        }
    }

    // a recursive method that takes the yaml and returns a map of translations
    // currently it can take:
    //  - a Map     which would call this method again for every value in that map
    //  - a List    which would create its own keys like stone.0 stone.1 stone.2
    //  - a String  which would be the end of the recursion and save it inside the final return map
    private fun flattenYaml(input: Any?, parentKey: String): Map<String, String> {
        val result = mutableMapOf<String, String>()

        when (input) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                for ((key, value) in input as Map<String, Any>) {
                    if(key == "init"){
                        result[parentKey] = value.toString()
                    }else{
                        val newKey = if (parentKey.isNotEmpty()) "$parentKey.$key" else key
                        result.putAll(flattenYaml(value, newKey))
                    }
                }
            }
            is List<*> -> {
                for ((index, value) in input.withIndex()) {
                    val newKey = "$parentKey.$index"
                    result.putAll(flattenYaml(value, newKey))
                }
            }
            else -> {
                result[parentKey] = input?.toString() ?: ""
            }
        }

        return result
    }

}

fun translate(text : String) : String{
    return Translator.translate(text) ?: text
}