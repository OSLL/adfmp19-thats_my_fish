package osll.thatsmyfish.game

import android.content.Context
import android.graphics.Canvas
import com.github.florent37.shapeofview.shapes.PolygonView
import osll.thatsmyfish.game.internal.Tile
import kotlin.math.min

class TileView(val tile: Tile, context: Context) : PolygonView(context) {
    init {
        noOfSides = tile.shape.sides
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (val player = tile.occupiedBy) {
            null -> {}
            else -> canvas.drawCircle(
                    width / 2f, height / 2f,
                    min(width, height) / 3f, GameFieldView.getPaint(player.color)
            )
        }

        canvas.drawText(
                tile.fishCount.toString(),
                width / 2f, height / 2f,
                GameFieldView.textPaint
        )
    }
}