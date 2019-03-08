package osll.thatsmyfish.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.*
import android.graphics.Path
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.widget.TextView
import com.github.florent37.shapeofview.ShapeOfView
import com.github.florent37.shapeofview.manager.ClipPathManager
import com.github.florent37.shapeofview.shapes.PolygonView
import com.github.florent37.shapeofview.shapes.TriangleView
import osll.thatsmyfish.game.internal.*
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

private const val side = 200f

private class TileView(
        private val shape: Shape, flipVertical: Boolean, context: Context
): ShapeOfView(context) {
    init {
        setClipPathCreator(object : ClipPathManager.ClipPathCreator {
            override fun requiresBitmap() = false

            override fun createClipPath(width: Int, height: Int): Path {
                val points = shape.run {
                    getPoints(width.toFloat(), height.toFloat(), flipVertical)
                }

                val path = Path()

                path.moveTo(points[0], points[1])
                for (i in 1 until points.size / 2) {
                    path.lineTo(points[2 * i], points[2 * i + 1])
                }
                path.close()

                return path
            }
        })
    }

    private fun exactSpec(value: Float) =
            MeasureSpec.makeMeasureSpec(value.toInt(), EXACTLY)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (shape) {
            Rectangle -> super.onMeasure(
                    exactSpec(side),
                    exactSpec(side)
            )
            Triangle -> super.onMeasure(
                    exactSpec(side),
                    exactSpec(sin(PI / 3).toFloat() * side)
            )
            Hexagon -> super.onMeasure(
                    exactSpec(side),
                    exactSpec(side / 2 * tan(PI / 3).toFloat())
            )
        }
    }
}

fun createTileView(tile: Tile, context: Context, flipVertical: Boolean): ShapeOfView {
    val baseView = TileView(tile.shape, flipVertical, context)

    val textView = object : TextView(context) {
        override fun onDraw(canvas: Canvas) {
            when (val mode = backgroundTintMode) {
                null -> canvas.drawColor(Color.CYAN)
                else -> canvas.drawColor(Color.CYAN, mode)
            }

            when (val player = tile.occupiedBy) {
                null -> {
                }
                else -> canvas.drawCircle(
                        width / 2f, height / 2f,
                        min(width, height) / 3f, GameActivity.getPaint(player.color)
                )
            }

            super.onDraw(canvas)
        }
    }

    textView.text = tile.fishCount.toString()
    textView.setBackgroundColor(TRANSPARENT)
    textView.setTextColor(BLACK)

    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    textView.gravity = Gravity.CENTER

    baseView.addView(textView)

    return baseView
}