package me.wanttobee.wtbmGameLib.imGui

import imgui.ImBool
import imgui.ImDouble
import imgui.ImFloat
import imgui.ImGui
import imgui.ImInt
import imgui.enums.ImGuiColorEditFlags
import imgui.enums.ImGuiCond
import me.wanttobee.wtbmGameLib.Logger
import org.joml.Vector3f

// Beware!! this shouldn't be used in any finished product
// because of its implementation it is really easy to use but REALLY inefficient
// if you want some widget in your product you will have to make your own  implementation
// that doesn't recreate the option every iteration

// so this object is a pre initialized GuiWidget which is really easy to use
// it makes it really easy to just tweak a little number without having to constantly reset the code
// for example:
//  `height : Int = 10`
// can just be replaced with
//  `height : Int = DevGui.int(10)`
// this returns 10 if you don't do anything, but it also initializes an option in the dev gui where you
// can modify this number
// the different options are:
//  `DevGui.checkBox(title, initial = false)`
//  `DevGui.button(title)`
//  `DevGui.color(reference, title)` Beware!! this one edits the color you enter as parameter
//  `DevGui.int(initial, title)`
//  `DevGui.float(initial, title)`
//  `DevGui.double(initial, title)`

object DevGui : IImGuiWidget {
    private val optionsMap : MutableMap<String,()-> Unit> = mutableMapOf()
    private val resultHash : MutableMap<String, Any> = mutableMapOf()

    override fun renderUi() {
        if(optionsMap.isEmpty()) return
        ImGui.setNextWindowSize(240f, optionsMap.size * 25f + 32f, ImGuiCond.Once)
        ImGui.setNextWindowPos(0f,0f, ImGuiCond.Once)
        ImGui.begin("WTBM Dev Gui")

        resultHash.clear()
        optionsMap.forEach { (_, option) ->
            option.invoke()
        }
        optionsMap.clear()

        ImGui.end()
    }

    private fun optionAlreadyInMap(title:String) : Boolean{
        if(optionsMap.containsKey(title)){
            optionsMap.remove(title)
            Logger.logWarning("you cant have 2 DevGui options with the same name '$title', if you want this you will have to make your own gui widget", true)
            return true
        }
        return false
    }

    fun checkBox(title : String = "checkBox", initialValue: Boolean = false) : Boolean{
        if(optionAlreadyInMap(title))
            return initialValue
        val result = resultHash[title]
        val currentlyChecked = if(result is Boolean) result else initialValue
        optionsMap[title] = {
            val someBool = ImBool(currentlyChecked)
            ImGui.checkbox(title, someBool)
            resultHash[title] = someBool.get()
        }
        return currentlyChecked
    }

    fun button(title : String = "button") : Boolean{
        if(optionAlreadyInMap(title))
            return false
        optionsMap[title] = {
            resultHash[title] = ImGui.button(title)
        }
        val result = resultHash[title]
        return if(result is Boolean) result else false
    }


    fun color(reference : Vector3f, title: String = "color picker") : Vector3f{
        if(optionAlreadyInMap(title))
            return reference
        optionsMap[title] = {
            val color = floatArrayOf(reference.x, reference.y,reference.z)
            ImGui.colorEdit3(
                "##color_picker", color,
                ImGuiColorEditFlags.NoInputs or ImGuiColorEditFlags.NoDragDrop
            )
            reference.x = color[0]
            reference.y = color[1]
            reference.z = color[2]
        }
        return reference
    }

    fun int(initialValue: Int, title: String = "int var"): Int {
        if(optionAlreadyInMap(title))
            return initialValue
        val result = resultHash[title]
        val newValue = if(result is Int) result else initialValue
        optionsMap[title] = {
            val intValue = ImInt(newValue)
            ImGui.inputInt(title, intValue)
            resultHash[title] = intValue.get()
        }
        return newValue
    }

    fun float(initialValue: Float, title : String = "float var"): Float {
        if(optionAlreadyInMap(title))
            return initialValue
        val result = resultHash[title]
        val newValue = if(result is Float) result else initialValue
        optionsMap[title] = {
            val floatValue = ImFloat(newValue)
            ImGui.inputFloat(title, floatValue)
            resultHash[title] = floatValue.get()
        }
        return newValue
    }

    fun double(initialValue: Double, title : String = "double var"): Double {
        if(optionAlreadyInMap(title))
            return initialValue
        val result = resultHash[title]
        val newValue = if(result is Double) result else initialValue
        optionsMap[title] = {
            val doubleValue = ImDouble(newValue)
            ImGui.inputDouble(title, doubleValue)
            resultHash[title] = doubleValue.get()
        }
        return newValue
    }
}

