package osll.thatsmyfish.game

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.text.Layout
import android.text.TextPaint
import android.util.Log
import android.util.Size
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import osll.thatsmyfish.R
import kotlinx.android.synthetic.main.activity_game.*
import osll.thatsmyfish.game.internal.AsyncPlayer
import osll.thatsmyfish.game.internal.Bot
import osll.thatsmyfish.game.internal.GameHandler
import osll.thatsmyfish.game.internal.Shape

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_game)

        val playerNames = intent.getStringArrayExtra("players")
        val bots = intent.getIntExtra("botCount", 0)
        val fieldSize = Size(
                intent.getIntExtra("fieldWidth", 5),
                intent.getIntExtra("fieldHeight", 5)
        )
        val chosenShape = intent.getStringExtra("tileShape")

        val playerColors: Array<Int> = resources.getIntArray(R.array.player_colors).toTypedArray()
        val humanPlayers = playerNames.zip(playerColors).map { AsyncPlayer(it.first, it.second) }
        val botPlayers = List(bots) { "Bot #$it" }.zip(playerColors.drop(playerNames.size)).map { Bot(it
                    .first, it.second) }
        val players = humanPlayers + botPlayers

        val gameHandler = GameHandler(fieldSize, Shape.byName(chosenShape), players)

        val scoresView = findViewById<LinearLayout>(R.id.game_scores)
        for (player in players) {
            val playerView = PlayerView(this, player) { gameHandler.getScore(player) }
            scoresView.addView(playerView)
        }

        val gameHolder = findViewById<ScrollView>(R.id.game_view_holder)
        gameHolder.addView(GameFieldView(this, gameHandler))
    }

    companion object {
        private val paints: MutableMap<Int, Paint> = mutableMapOf()

        fun getPaint(color: Int): Paint = paints.getOrPut(
                color
        ) {
            val paint = Paint()
            paint.color = color

            paint
        }

        val textPaint: TextPaint

        init {
            textPaint = TextPaint(getPaint(Color.BLACK))
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize *= 10
        }
    }
}
