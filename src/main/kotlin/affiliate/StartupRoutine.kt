package dev.cdh.affiliate

import dev.cdh.WINDOW_SIZE
import java.io.File
import java.util.Properties

/** One cat to spawn at startup: a [type] (color) and an optional fixed [name]. */
data class CatSpec(val type: String, val name: String?)

/**
 * The persisted startup routine — which cats (and what size) the app spawns when it launches.
 * Edited from the tray "Startup" menu and stored as a plain `.properties` file (no dependencies)
 * under `$XDG_CONFIG_HOME/catppuccino/startup.properties`. Read once by `Main` at launch.
 */
object StartupRoutine {
    private val configFile: File by lazy {
        val base = System.getenv("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() }
            ?: "${System.getProperty("user.home")}/.config"
        File(base, "catppuccino/startup.properties")
    }

    val entries = mutableListOf<CatSpec>()
    var size: Int = 100
        private set

    fun exists(): Boolean = configFile.exists()

    fun load() {
        if (!configFile.exists()) return
        val props = Properties()
        configFile.inputStream().use { props.load(it) }
        size = props.getProperty("size")?.toIntOrNull() ?: size
        entries.clear()
        props.getProperty("cats").orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { spec ->
                val parts = spec.split(":", limit = 2)
                entries.add(CatSpec(parts[0], parts.getOrNull(1)?.takeIf { it.isNotBlank() }))
            }
    }

    private fun save() {
        val props = Properties()
        props.setProperty("size", size.toString())
        props.setProperty("cats", entries.joinToString(",") { spec ->
            if (spec.name.isNullOrBlank()) spec.type else "${spec.type}:${spec.name}"
        })
        configFile.parentFile?.mkdirs()
        configFile.outputStream().use { props.store(it, "Catppuccino startup routine") }
    }

    /** Append a cat of [type], giving it [name] or a fresh pool name unused within the routine. */
    fun add(type: String, name: String? = null) {
        val chosen = name?.trim()?.takeIf { it.isNotEmpty() }?.let { CatNames.unique(it, usedNames()) }
            ?: CatNames.randomUnused(usedNames())
        entries.add(CatSpec(type, chosen))
        save()
    }

    fun rename(index: Int, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || index !in entries.indices) return
        entries[index] = entries[index].copy(name = CatNames.unique(trimmed, usedNames(index)))
        save()
    }

    fun removeAt(index: Int) {
        if (index in entries.indices) {
            entries.removeAt(index)
            save()
        }
    }

    fun clear() {
        entries.clear()
        save()
    }

    /** Snapshot the cats currently on screen (types, names) and the current size into the routine. */
    fun captureCurrent() {
        entries.clear()
        CatManager.cats().forEach { entries.add(CatSpec(it.catType, it.name.takeIf { n -> n.isNotBlank() })) }
        size = WINDOW_SIZE
        save()
    }

    private fun usedNames(exclude: Int? = null): Set<String> =
        entries.filterIndexed { i, _ -> i != exclude }.mapNotNull { it.name }.toSet()
}
