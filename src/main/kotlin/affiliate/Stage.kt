package dev.cdh.affiliate

import java.awt.Graphics
import javax.swing.JPanel

internal class Stage(private val cat: Cat) : JPanel() {
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(cat.currentRender.image, 0, 0, null)
    }
}
