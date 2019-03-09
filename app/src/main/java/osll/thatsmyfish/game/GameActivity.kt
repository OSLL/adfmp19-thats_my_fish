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
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import osll.thatsmyfish.R
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.SelectClause1
import kotlinx.coroutines.selects.select
import osll.thatsmyfish.game.internal.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameActivity : AppCompatActivity() {
    private lateinit var gameView: GameFieldView

    private val scoresView
        get() = findViewById<LinearLayout>(R.id.game_scores)
    private lateinit var playerViews: Map<Player, PlayerView>

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

        playerViews = players.map {
            it to PlayerView(this, it) { gameHandler.getScore(it) }
        }.toMap()

        for ((_, playerView) in playerViews) {
            scoresView.addView(playerView)
        }

        val gameHolder = findViewById<ScrollView>(R.id.game_view_holder)
        gameView = GameFieldView(this, gameHandler)
        gameHolder.addView(gameView)


        GlobalScope.launch {
            var finished = false

            while (!finished) {
                val activePlayerScoresView = gameHandler.activePlayer?.let {
                    playerViews.getValue(it)
                }
                activePlayerScoresView?.apply {
                    paint.style = Paint.Style.FILL_AND_STROKE
                    paint.strokeWidth = 2f
                    postInvalidate()
                }

                val turnInfo = gameHandler.handleTurn()

                finished = finished
                        || (turnInfo is PhaseStarted && turnInfo.gameState is Finished)

                activePlayerScoresView?.apply {
                    paint.style = Paint.Style.FILL
                    postInvalidate()
                }

                runOnUiThread {
                    this@GameActivity.handleGameFieldUpdate(turnInfo)
                }
            }
        }
    }

    private fun showToast(message: String) =
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun handleGameFieldUpdate(turnInfo: TurnInfo) {
        when (turnInfo) {
            InvalidTurn      -> {}
            is PenguinPlaced -> {
                gameView.viewByTile(turnInfo.tile).postInvalidate()
                playerViews.getValue(turnInfo.player).postInvalidate()
            }
            is PenguinMoved  -> {
                gameView.viewByTile(turnInfo.fromTile).visibility = INVISIBLE
                gameView.viewByTile(turnInfo.toTile).textView.postInvalidate()
                playerViews.getValue(turnInfo.player).postInvalidate()
            }
            is PhaseStarted  -> when (turnInfo.gameState) {
                InitialPlacement -> showToast("Time to place your penguins!")
                Running          -> showToast("All penguins placed!")
                is Finished      -> showToast(
                        "${turnInfo.gameState.gameStats.scores.first().first.name} wins!"
                )
            }
            is PlayerFinished    -> {
                showToast("${turnInfo.player.name} has no more turns")
            }
        }
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

        val textPaint = TextPaint(getPaint(Color.BLACK)).apply {
            textAlign = Paint.Align.CENTER
            textSize *= 10
        }
    }
}
