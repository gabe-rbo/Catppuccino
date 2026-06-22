package dev.cdh.affiliate

import dev.cdh.WINDOW_SIZE

/**
 * Single source of truth for the live cats. The tray menu mutates cats only through here; whenever
 * the cat set or a toggle changes, [onCatsChanged] is fired so the tray can rebuild its menu. Every
 * call happens on the EDT (Swing [javax.swing.Timer] ticks and AWT menu events both dispatch there),
 * so the registry needs no synchronization.
 */
object CatManager {
    private val controllers = mutableListOf<CatController>()

    var sleeping = false
        private set
    var namesVisible = false
        private set

    /** Wired by [SystemTrayManager] to rebuild the popup menu after any change. */
    var onCatsChanged: (() -> Unit)? = null

    fun cats(): List<Cat> = controllers.map { it.cat }

    fun spawn(type: String, name: String? = null) {
        val cat = Cat(ResourcesLoader(type))
        cat.setName(name?.trim()?.takeIf { it.isNotEmpty() }?.let { uniqueName(it) } ?: randomUnusedName())
        val controller = CatController(cat)
        controllers.add(controller)
        controller.start()
        if (sleeping) cat.asleep = true
        if (namesVisible) cat.setNameVisible(true)
        onCatsChanged?.invoke()
    }

    fun remove(cat: Cat) {
        val controller = controllers.find { it.cat === cat } ?: return
        controller.dispose()
        controllers.remove(controller)
        onCatsChanged?.invoke()
    }

    fun removeAll() {
        controllers.forEach { it.dispose() }
        controllers.clear()
        onCatsChanged?.invoke()
    }

    fun rename(cat: Cat, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        cat.setName(uniqueName(trimmed, cat))
        onCatsChanged?.invoke()
    }

    fun setSleeping(value: Boolean) {
        sleeping = value
        controllers.forEach { it.cat.asleep = value }
    }

    fun setNamesVisible(value: Boolean) {
        namesVisible = value
        controllers.forEach { it.cat.setNameVisible(value) }
    }

    fun setSize(px: Int) {
        WINDOW_SIZE = px
        controllers.forEach { it.cat.resize(px) }
    }

    private fun usedNames(exclude: Cat? = null): Set<String> =
        controllers.asSequence().map { it.cat }.filter { it !== exclude }.map { it.name }.toSet()

    private fun randomUnusedName(): String = CatNames.randomUnused(usedNames())

    private fun uniqueName(desired: String, exclude: Cat? = null): String =
        CatNames.unique(desired, usedNames(exclude))
}
