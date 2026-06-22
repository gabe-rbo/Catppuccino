package dev.cdh.affiliate

import dev.cdh.WINDOW_SIZE
import java.awt.*
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JOptionPane

internal object SystemTrayManager {
    private const val PROJECT_NAME = "Catppuccino"
    private val SIZE_PRESETS = listOf(75, 100, 125, 150, 200)
    private val SPAWN_TYPES = listOf(
        "calico_cat" to "Calico",
        "grey_tabby_cat" to "Grey Tabby",
        "orange_cat" to "Orange",
        "white_cat" to "White",
    )

    private var trayIcon: TrayIcon? = null

    @Throws(RuntimeException::class)
    fun initialize() {
        if (!SystemTray.isSupported()) return

        try {
            val icon = createTrayIcon()
            trayIcon = icon
            CatManager.onCatsChanged = ::rebuildMenu
            icon.popupMenu = buildMenu()
            SystemTray.getSystemTray().add(icon)
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize system tray", e)
        }
    }

    /** Rebuilds the popup so checkmarks and the per-cat submenus track the live state. */
    fun rebuildMenu() {
        trayIcon?.popupMenu = buildMenu()
    }

    @Throws(IOException::class)
    private fun createTrayIcon(): TrayIcon {
        val iconSize = SystemTray.getSystemTray().trayIconSize

        val image = ImageIO.read(
            Objects.requireNonNull(
                SystemTrayManager::class.java.classLoader.getResourceAsStream("$PROJECT_NAME.png")
            )
        ).getScaledInstance(iconSize.width, iconSize.height, Image.SCALE_SMOOTH)

        return TrayIcon(image, PROJECT_NAME)
    }

    private fun buildMenu(): PopupMenu {
        val menu = PopupMenu()

        val sleep = CheckboxMenuItem("Sleep (all cats)", CatManager.sleeping)
        sleep.addItemListener { CatManager.setSleeping(sleep.state) }
        menu.add(sleep)

        val names = CheckboxMenuItem("Show names", CatManager.namesVisible)
        names.addItemListener { CatManager.setNamesVisible(names.state) }
        menu.add(names)

        menu.add(buildSizeMenu())
        menu.addSeparator()
        menu.add(buildSpawnMenu())

        val cats = CatManager.cats()
        if (cats.isNotEmpty()) {
            menu.add(buildRenameMenu(cats))
            menu.add(buildRemoveMenu(cats))
        }

        menu.addSeparator()
        menu.add(buildStartupMenu())

        // No "Exit" item on purpose: this is an always-on tray app. Stop it with
        // `systemctl --user stop catppuccino` (or kill the process) when you really want it gone.
        return menu
    }

    private fun colorLabel(type: String): String =
        SPAWN_TYPES.toMap()[type] ?: type.removeSuffix("_cat").replaceFirstChar { it.uppercase() }

    private fun buildSizeMenu(): Menu {
        val sizeMenu = Menu("Size")
        for (px in SIZE_PRESETS) {
            val item = CheckboxMenuItem("$px px", px == WINDOW_SIZE)
            item.addItemListener {
                CatManager.setSize(px)
                rebuildMenu()
            }
            sizeMenu.add(item)
        }
        return sizeMenu
    }

    private fun buildSpawnMenu(): Menu {
        val spawnMenu = Menu("Spawn")
        for ((type, label) in SPAWN_TYPES) {
            val item = MenuItem(label)
            item.addActionListener { CatManager.spawn(type) }
            spawnMenu.add(item)
        }
        val random = MenuItem("Random")
        random.addActionListener { CatManager.spawn(ResourcesLoader.CAT_TYPES.random()) }
        spawnMenu.add(random)
        return spawnMenu
    }

    private fun buildRenameMenu(cats: List<Cat>): Menu {
        val renameMenu = Menu("Rename")
        for (cat in cats) {
            val item = MenuItem(cat.name)
            item.addActionListener {
                val input = JOptionPane.showInputDialog(null, "New name for ${cat.name}:", cat.name)
                if (input != null) CatManager.rename(cat, input)
            }
            renameMenu.add(item)
        }
        return renameMenu
    }

    private fun buildRemoveMenu(cats: List<Cat>): Menu {
        val removeMenu = Menu("Remove")
        for (cat in cats) {
            val item = MenuItem(cat.name)
            item.addActionListener { CatManager.remove(cat) }
            removeMenu.add(item)
        }
        removeMenu.addSeparator()
        val all = MenuItem("Remove all")
        all.addActionListener { CatManager.removeAll() }
        removeMenu.add(all)
        return removeMenu
    }

    /** Edits the persisted startup routine — the cats spawned when the app launches at login. */
    private fun buildStartupMenu(): Menu {
        val startup = Menu("Startup")
        val entries = StartupRoutine.entries

        val summary = MenuItem(
            if (entries.isEmpty()) "Spawns no cats at launch (tray only)"
            else "Spawns at launch: " + entries.joinToString(", ") { it.name ?: colorLabel(it.type) }
        )
        summary.isEnabled = false
        startup.add(summary)
        startup.addSeparator()

        val add = Menu("Add")
        for ((type, label) in SPAWN_TYPES) {
            val item = MenuItem(label)
            item.addActionListener { StartupRoutine.add(type); rebuildMenu() }
            add.add(item)
        }
        val random = MenuItem("Random")
        random.addActionListener { StartupRoutine.add(ResourcesLoader.CAT_TYPES.random()); rebuildMenu() }
        add.add(random)
        startup.add(add)

        if (entries.isNotEmpty()) {
            val rename = Menu("Rename")
            val remove = Menu("Remove")
            entries.forEachIndexed { index, spec ->
                val label = "${spec.name ?: "(unnamed)"} — ${colorLabel(spec.type)}"
                MenuItem(label).also { item ->
                    item.addActionListener {
                        val input = JOptionPane.showInputDialog(null, "New startup name:", spec.name ?: "")
                        if (input != null) { StartupRoutine.rename(index, input); rebuildMenu() }
                    }
                    rename.add(item)
                }
                MenuItem(label).also { item ->
                    item.addActionListener { StartupRoutine.removeAt(index); rebuildMenu() }
                    remove.add(item)
                }
            }
            remove.addSeparator()
            val clear = MenuItem("Remove all")
            clear.addActionListener { StartupRoutine.clear(); rebuildMenu() }
            remove.add(clear)
            startup.add(rename)
            startup.add(remove)
        }

        startup.addSeparator()
        val capture = MenuItem("Use current on-screen cats")
        capture.addActionListener { StartupRoutine.captureCurrent(); rebuildMenu() }
        startup.add(capture)

        return startup
    }
}
