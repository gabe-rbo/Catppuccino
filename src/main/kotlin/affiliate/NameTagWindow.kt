package dev.cdh.affiliate

import dev.cdh.SCREEN_BOUNDS
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Window
import java.awt.geom.RoundRectangle2D
import javax.swing.JPanel
import javax.swing.JWindow

/**
 * A small always-on-top label that floats above a cat showing its name. Like [CatWindow] it relies
 * on a shaped (non-alpha) window — an opaque rounded-rectangle pill — rather than per-pixel
 * translucency, so it renders correctly on the second monitor, which cannot composite window alpha
 * on this machine. The pill is resized and re-shaped to fit the current name.
 */
class NameTagWindow {
    private val window = JWindow()
    private val panel = TagPanel()
    private var text = ""

    init {
        window.type = Window.Type.UTILITY
        window.isAlwaysOnTop = true
        window.focusableWindowState = false // never steal focus or intercept clicks
        window.contentPane = panel
    }

    fun setText(value: String) {
        text = value
        resizeToText()
    }

    private fun resizeToText() {
        val fm = window.getFontMetrics(FONT)
        val w = fm.stringWidth(text) + PAD_X * 2
        val h = fm.height + PAD_Y * 2
        window.size = Dimension(w, h)
        window.shape = RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), ARC, ARC)
        window.repaint()
    }

    /** Centres the pill horizontally over the cat and sits it just above, clamped on-screen. */
    fun updatePosition(catLocation: Point, catSize: Dimension) {
        val w = window.width
        val h = window.height
        if (w == 0 || h == 0) return
        val bounds = SCREEN_BOUNDS
        val x = (catLocation.x + (catSize.width - w) / 2).coerceIn(bounds.x, bounds.x + bounds.width - w)
        val y = (catLocation.y - h - GAP).coerceAtLeast(bounds.y)
        window.setLocation(x, y)
    }

    var isVisible: Boolean
        get() = window.isVisible
        set(value) { window.isVisible = value }

    fun dispose() = window.dispose()

    private inner class TagPanel : JPanel() {
        init {
            isOpaque = false
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2 = g as Graphics2D
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g2.color = BG
            g2.fillRoundRect(0, 0, width, height, ARC.toInt(), ARC.toInt())
            g2.color = FG
            g2.font = FONT
            val fm = g2.fontMetrics
            val tx = (width - fm.stringWidth(text)) / 2
            val ty = (height - fm.height) / 2 + fm.ascent
            g2.drawString(text, tx, ty)
        }
    }

    private companion object {
        val FONT = Font(Font.SANS_SERIF, Font.BOLD, 12)
        const val PAD_X = 10
        const val PAD_Y = 5
        const val ARC = 12f
        const val GAP = 4 // gap between the pill and the cat
        val BG: Color = Color(40, 40, 40)
        val FG: Color = Color(245, 245, 245)
    }
}
