package dev.cdh

import dev.cdh.constants.Behave
import java.awt.*
import kotlin.math.abs
import kotlin.random.Random

var WINDOW_SIZE = 100
val SCREEN_BOUNDS: Rectangle
    get() {
        var virtualBounds = Rectangle()
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        for (screen in ge.screenDevices) {
            virtualBounds = virtualBounds.union(screen.defaultConfiguration.bounds)
        }
        return virtualBounds
    }

fun Point.generateRandomTarget(windowSize: Dimension): Point {
    val bounds = SCREEN_BOUNDS
    var target: Point
    do {
        target = Point(
            Random.nextInt(bounds.width - windowSize.width - 20) + bounds.x + 10,
            Random.nextInt(bounds.height - windowSize.height - 20) + bounds.y + 10
        )
    } while (abs(this.y - target.y) <= 400 &&
        abs(this.x - target.x) <= 400
    )

    return target
}

fun Point.clampToScreen(bounds: Rectangle, windowSize: Dimension) {
    x = x.coerceIn(bounds.x - 10, bounds.x + bounds.width - windowSize.width)
    y = y.coerceIn(bounds.y - 35, bounds.y + bounds.height - windowSize.height)
}

fun Point.move(action: Behave) {
    when (action) {
        Behave.RIGHT -> this.translate(1, 0)
        Behave.LEFT -> this.translate(-1, 0)
        Behave.UP -> this.translate(0, -1)
        Behave.DOWN -> this.translate(0, 1)
        else -> {}
    }
}
