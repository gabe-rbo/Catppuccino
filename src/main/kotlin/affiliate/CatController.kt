package dev.cdh.affiliate

import dev.cdh.constants.Behave
import java.time.LocalDateTime
import javax.swing.Timer

class CatController(val cat: Cat) {
    private var wanderCount = 0
    private val wanderInterval: Int by lazy(LazyThreadSafetyMode.NONE) {
        val hour = LocalDateTime.now().hour
        if (hour in 8..<18) 600 else 3000
    }
    private lateinit var timer: Timer

    fun start() {
        cat.window.isVisible = true
        cat.changeAction(Behave.CURLED)
        timer = Timer(20) {
            cat.update()
            if (++wanderCount >= wanderInterval) {
                cat.tryWandering()
                wanderCount = 0
            }
        }
        timer.start()
    }

    fun dispose() {
        if (::timer.isInitialized) timer.stop()
        cat.dispose()
    }
}