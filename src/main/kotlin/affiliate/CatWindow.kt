package dev.cdh.affiliate

import dev.cdh.WINDOW_SIZE
import dev.cdh.constants.Behave
import dev.cdh.constants.BubbleState
import java.awt.Dimension
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JWindow

/**
 * Hosts a single cat. The window is clipped to the cat's silhouette via [Window.setShape] (the X
 * Shape extension) rather than per-pixel translucency: alpha-composited windows render black on the
 * second monitor here and lose their alpha when dragged across GraphicsDevices, whereas a shaped
 * window stays correct on every monitor. The shape is only re-applied when the frame actually
 * changes (tracked by [appliedShapeKey]).
 */
class CatWindow(private val cat: Cat) {
    private val window = JWindow()
    private var appliedShapeKey: String? = null

    init {
        window.type = Window.Type.UTILITY
        window.setSize(WINDOW_SIZE, WINDOW_SIZE)
        window.preferredSize = Dimension(WINDOW_SIZE, WINDOW_SIZE)
        window.isAlwaysOnTop = true
        window.add(Stage(cat))
        window.setLocationRelativeTo(null)
        setupMouseListeners()
    }

    private fun applyShape() {
        val render = cat.currentRender
        if (render.key != appliedShapeKey) {
            window.shape = render.shape
            appliedShapeKey = render.key
        }
    }

    private fun setupMouseListeners() {
        val adapter = object : MouseAdapter() {
            private val dragOffset = Point(0, 0)

            override fun mousePressed(e: MouseEvent) {
                dragOffset.setLocation(e.x, e.y)
            }

            override fun mouseDragged(e: MouseEvent) {
                window.setLocation(e.locationOnScreen.x - dragOffset.x, e.locationOnScreen.y - dragOffset.y)
                if (cat.changeAction(Behave.RISING)) {
                    cat.animationState.resetFrame()
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                if (cat.currentAction == Behave.RISING) {
                    cat.changeAction(Behave.LAYING)
                    cat.animationState.resetFrame()
                }
            }

            override fun mouseClicked(e: MouseEvent?) {
                cat.bubbleState = BubbleState.HEART
            }
        }
        window.addMouseListener(adapter)
        window.addMouseMotionListener(adapter)
    }

    fun setSize(size: Int) {
        window.setSize(size, size)
        window.preferredSize = Dimension(size, size)
        repaint() // re-clip; the next render at the new WINDOW_SIZE refreshes the sprite
    }

    fun dispose() {
        window.dispose()
    }

    var location: Point
        get() = window.location
        set(value) { window.location = value }

    val locationOnScreen: Point get() = window.locationOnScreen

    val size: Dimension get() = window.size

    var isVisible: Boolean
        get() = window.isVisible
        set(value) {
            if (value) applyShape() // clip before first show so no full rectangle flashes
            window.isVisible = value
        }

    fun repaint() {
        applyShape()
        window.repaint()
    }
}
