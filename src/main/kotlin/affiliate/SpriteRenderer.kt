package dev.cdh.affiliate

import dev.cdh.ImageCache
import dev.cdh.WINDOW_SIZE
import dev.cdh.constants.Behave
import dev.cdh.constants.BubbleState
import dev.cdh.constants.Direction
import java.awt.Graphics2D
import java.awt.Point
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

/** A pre-rendered animation frame: the binarized sprite image plus the matching window outline. */
class RenderedFrame(val image: BufferedImage, val shape: Shape, val key: String)

/**
 * Renders a cat's current frame (sprite + speech bubble) into a single opaque image and the outline
 * of its non-transparent pixels. The window is clipped to that outline via the X Shape extension
 * instead of using per-pixel window translucency: alpha-composited windows render black on the
 * second monitor on this machine and lose their alpha when dragged across GraphicsDevices, whereas
 * a shaped window stays correct on every monitor regardless of the compositor. Results are cached
 * per distinct frame, so the per-pixel work runs once per unique sprite.
 */
object SpriteRenderer {
    private const val ALPHA_THRESHOLD = 128
    private val cache = HashMap<String, RenderedFrame>()

    fun render(cat: Cat): RenderedFrame {
        val state = cat.animationState
        val flip = needsFlipping(cat.currentAction, cat.layingDir)
        val bubbleFrame = if (cat.bubbleState == BubbleState.NONE) 0 else state.bubbleFrame
        val key = "${cat.catType}|${cat.currentAction}|${state.frameNum}|$flip|" +
                "${cat.layingDir}|${cat.bubbleState}|$bubbleFrame|$WINDOW_SIZE"
        cache[key]?.let { return it }

        val image = BufferedImage(WINDOW_SIZE, WINDOW_SIZE, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
        drawCat(g, cat, flip)
        drawBubble(g, cat)
        g.dispose()

        val frame = RenderedFrame(image, binarizeAndBuildShape(image), key)
        cache[key] = frame
        return frame
    }

    private fun drawCat(g: Graphics2D, cat: Cat, flip: Boolean) {
        val frames = cat.currentFrames
        if (frames.isEmpty()) return
        val idx = cat.animationState.frameNum.coerceIn(0, frames.size - 1)
        var img = frames[idx] ?: return
        if (flip) img = ImageCache.getOrFlip(img, "${cat.catType}:${cat.currentAction.name}$idx")
        g.drawImage(img, 0, 0, WINDOW_SIZE, WINDOW_SIZE, null)
    }

    private fun drawBubble(g: Graphics2D, cat: Cat) {
        if (cat.bubbleState == BubbleState.NONE) return
        val frames = cat.currentBubbleFrames
        if (frames.isEmpty()) return
        val idx = cat.animationState.bubbleFrame.coerceIn(0, frames.size - 1)
        val bubble = frames[idx] ?: return
        val size = (WINDOW_SIZE * 0.3).toInt()
        val pos = bubblePosition(cat.currentAction, cat.layingDir)
        g.drawImage(bubble, pos.x, pos.y, size, size, null)
    }

    private fun needsFlipping(action: Behave, direction: Direction): Boolean =
        (action == Behave.LAYING || action == Behave.RISING || action == Behave.SLEEP) && direction == Direction.LEFT ||
                action == Behave.CURLED && direction == Direction.RIGHT

    private fun bubblePosition(action: Behave, direction: Direction): Point {
        val baseX = (WINDOW_SIZE * 0.3).toInt()
        val baseY = (WINDOW_SIZE * 0.4).toInt()
        val offsetX = (WINDOW_SIZE * 0.3).toInt()
        val offsetY = (WINDOW_SIZE * 0.25).toInt()
        return when (action) {
            Behave.SLEEP, Behave.LAYING, Behave.LEFT, Behave.RIGHT ->
                Point(if (direction == Direction.LEFT) 0 else baseX + offsetX, baseY)
            Behave.UP, Behave.LICKING, Behave.SITTING -> Point(baseX, baseY - offsetY)
            else -> Point(baseX, baseY)
        }
    }

    /**
     * Forces every pixel to fully opaque or fully transparent (threshold on alpha) so the clipped
     * edges are crisp, and returns the outline of the opaque pixels as a union of horizontal runs.
     */
    private fun binarizeAndBuildShape(image: BufferedImage): Shape {
        val w = image.width
        val h = image.height
        val path = Path2D.Float(Path2D.WIND_NON_ZERO)
        for (y in 0 until h) {
            var runStart = -1
            for (x in 0 until w) {
                val argb = image.getRGB(x, y)
                if (argb ushr 24 and 0xFF >= ALPHA_THRESHOLD) {
                    image.setRGB(x, y, argb or (0xFF shl 24)) // force fully opaque
                    if (runStart < 0) runStart = x
                } else {
                    if (argb != 0) image.setRGB(x, y, argb and 0x00FFFFFF) // force fully transparent
                    if (runStart >= 0) {
                        path.append(Rectangle2D.Float(runStart.toFloat(), y.toFloat(), (x - runStart).toFloat(), 1f), false)
                        runStart = -1
                    }
                }
            }
            if (runStart >= 0) {
                path.append(Rectangle2D.Float(runStart.toFloat(), y.toFloat(), (w - runStart).toFloat(), 1f), false)
            }
        }
        return path
    }
}
