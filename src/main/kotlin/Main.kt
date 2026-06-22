package dev.cdh

import dev.cdh.affiliate.Cat
import dev.cdh.affiliate.CatController
import dev.cdh.affiliate.ResourcesLoader
import dev.cdh.affiliate.SystemTrayManager
import javax.swing.SwingUtilities

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val argList = args.toMutableList()
        val sizeIndex = argList.indexOf("--size")
        if (sizeIndex != -1 && sizeIndex + 1 < argList.size) {
            WINDOW_SIZE = argList[sizeIndex + 1].toInt()
            argList.removeAt(sizeIndex + 1)
            argList.removeAt(sizeIndex)
        }
        val catTypes = argList.ifEmpty { mutableListOf(ResourcesLoader.CAT_TYPES.random()) }

        SwingUtilities.invokeLater {
            SystemTrayManager.initialize()
            for (type in catTypes) {
                val loader = ResourcesLoader(type)
                val cat = Cat(loader)
                val controller = CatController(cat)
                controller.start()
            }
        }
    }
}