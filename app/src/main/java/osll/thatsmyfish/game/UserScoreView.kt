package osll.thatsmyfish.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_game.view.*
import osll.thatsmyfish.R

/**
 * View, containing a single player's score
 */
class UserScoreView(
        context: Context,
        @ColorInt private val color: Int,
        val name: String
) : TextView(context) {
    var score: Int = 0

    init {
        setBackgroundColor(color)
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        updateText()
    }

    private fun updateText() {
        text = "$name:\n$score"
    }

    fun addScore(delta: Int) {
        score += delta
        updateText()
    }
}
