package osll.thatsmyfish.game

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.view.Gravity
import android.widget.TextView
import osll.thatsmyfish.R
import osll.thatsmyfish.game.internal.AbstractPlayer
import kotlin.math.max

class PlayerView(
        context: Context,
        val player: AbstractPlayer,
        val scoreGetter: () -> Int
) : TextView(context) {
    private val screenSize = Point()

    init {
        setBackgroundColor(player.color)
        gravity = Gravity.CENTER

        (context as Activity).windowManager.defaultDisplay.getSize(screenSize)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val side = max(
                resources.getDimensionPixelSize(R.dimen.min_scores_side),
                screenSize.x / 4
        )
        setMeasuredDimension(side, side)
    }

    override fun onDraw(canvas: Canvas?) {
        text = resources.getString(R.string.player_score, player.name, scoreGetter())

        super.onDraw(canvas)
    }
}