package osll.thatsmyfish.game

import android.content.Context
import android.graphics.*
import android.graphics.Color.*
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.florent37.shapeofview.ShapeOfView
import com.github.florent37.shapeofview.manager.ClipPathManager
import osll.thatsmyfish.R
import osll.thatsmyfish.game.internal.*
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.tan

class TileView(
        val tile: Tile, val flipVertical: Boolean, context: Context
): ShapeOfView(context) {
    private val tintColor = ContextCompat.getColor(
            context,
            R.color.black_overlay
    )

    val textView = object : TextView(context) {
        init {
            setBackgroundColor(TRANSPARENT)
            setTextColor(BLACK)

            textAlignment = View.TEXT_ALIGNMENT_GRAVITY
            gravity = Gravity.CENTER
        }

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
                        min(width, height) / 4f, GameActivity.getPaint(player.color)
                )
            }

            foregroundTintMode?.let { canvas.drawColor(tintColor, it) }

            super.onDraw(canvas)
        }
    }

    private val clipPathCreator = TileClipPathCreator()

    init {
        setClipPathCreator(clipPathCreator)
        textView.text = tile.fishCount.toString()
        addView(textView)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?) = true

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        canvas.drawPath(
                clipPathCreator.createClipPath(width, height),
                GameActivity.getPaint(Color.BLACK).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
        )
    }

    override fun postInvalidate() {
        textView.postInvalidate()

        super.postInvalidate()
    }

    override fun invalidate() {
        textView.invalidate()

        super.invalidate()
    }

    fun setTint(darken: Boolean) {
        if (darken) {
            textView.foregroundTintMode = PorterDuff.Mode.DARKEN
        } else {
            textView.foregroundTintMode = null
        }
        invalidate()
    }

    inner class TileClipPathCreator : ClipPathManager.ClipPathCreator {
        override fun requiresBitmap() = false

        override fun createClipPath(width: Int, height: Int): Path {
            val points = tile.shape.run {
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
    }
}