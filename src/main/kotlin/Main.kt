package dev.cdh

import dev.cdh.affiliate.CatManager
import dev.cdh.affiliate.CatSpec
import dev.cdh.affiliate.ResourcesLoader
import dev.cdh.affiliate.StartupRoutine
import dev.cdh.affiliate.SystemTrayManager
import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val argList = args.toMutableList()

        var cliSize = false
        val sizeIndex = argList.indexOf("--size")
        if (sizeIndex != -1 && sizeIndex + 1 < argList.size) {
            WINDOW_SIZE = argList[sizeIndex + 1].toInt()
            argList.removeAt(sizeIndex + 1)
            argList.removeAt(sizeIndex)
            cliSize = true
        }
        val noCats = argList.remove("--no-cats")

        StartupRoutine.load()

        // Decide what to spawn:
        //   --no-cats            -> nothing (tray only)
        //   "type[:name]" args   -> exactly those (ad-hoc override)
        //   a saved routine      -> the configured cats (and size, unless --size was given)
        //   nothing saved        -> one random cat (first-run default)
        val specs: List<CatSpec> = when {
            noCats -> emptyList()
            argList.isNotEmpty() -> argList.map { entry ->
                entry.split(":", limit = 2).let { CatSpec(it[0], it.getOrNull(1)) }
            }
            StartupRoutine.exists() -> {
                if (!cliSize) WINDOW_SIZE = StartupRoutine.size
                StartupRoutine.entries.toList()
            }
            else -> listOf(CatSpec(ResourcesLoader.CAT_TYPES.random(), null))
        }

        SwingUtilities.invokeLater {
            SystemTrayManager.initialize()
            for (spec in specs) CatManager.spawn(spec.type, spec.name)
        }

        // Keep the JVM (and the tray icon) alive even with zero cats/windows.
        CountDownLatch(1).await()
    }
}
