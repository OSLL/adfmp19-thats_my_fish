package osll.thatsmyfish.game

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.TextPaint
import android.util.Size
import android.view.View.INVISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import osll.thatsmyfish.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private var gameType: GameType = GameType.SINGLE
    //private var gameStats: GameStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_game)

        val playerNames = intent.getStringArrayListExtra("playerNames")
        val bots = intent.getIntExtra("botCount", 0)
        val fieldSize = Size(
                intent.getIntExtra("fieldWidth", 5),
                intent.getIntExtra("fieldHeight", 5)
        )
        val chosenShape = intent.getStringExtra("tileShape")
        gameType = GameType.valueOf(intent.getStringExtra("gameType"))

        val playerColors: Array<Int> = resources.getIntArray(R.array.player_colors).toTypedArray()
        val humanPlayers = playerNames.zip(playerColors).map { AsyncPlayer(it.first, it.second) }
        val botPlayers = List(bots) { "Bot #$it" }.zip(playerColors.drop(playerNames.size)).map { Bot(it
                    .first, it.second) }
        val players = humanPlayers + botPlayers

        val gameHandler = GameHandler(
                fieldSize.width to fieldSize.height,
                Shape.byName(chosenShape),
                players,
                3
        )

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
                is Finished      -> {
                    val gameStats = turnInfo.gameState.gameStats
                    if (gameType == GameType.SINGLE) {
                        recalculateStats(gameStats)
                    }
                    val gameScores = gameStats.scores
                    val playerNames = gameScores.map { it.first.name }
                    val playerPoints = gameScores.map { it.second }
                    intent.removeExtra("playerNames")

                    startActivity(
                        Intent(
                                this, GameStatsActivity::class.java
                        ).apply {
                            putExtra("totalTime", gameStats.totalTime())
                            putExtra("totalMoves", gameStats.totalMoves)
                            putStringArrayListExtra("playerNames", playerNames.toCollection(ArrayList()))
                            putIntegerArrayListExtra("playerPoints", playerPoints.toCollection(ArrayList()))
                            putExtras(intent.extras!!)
                        })
                }
            }
            is PlayerFinished    -> {
                showToast("${turnInfo.player.name} has no more turns")
            }
        }
    }

    private fun recalculateStats(gameStats: GameStats) {
        val sharedPreferences = getSharedPreferences("stats", Context.MODE_PRIVATE)
        val win = sharedPreferences.getInt("win", 0)
        val draw = sharedPreferences.getInt("draw", 0)
        val lose = sharedPreferences.getInt("lose", 0)
        val scores = gameStats.scores
        var playerScore = 0
        for (p in scores) {
            val player = p.first
            if (player.name == "You") {
                playerScore = p.second
                break
            }
        }
        when {
            playerScore > scores[1].second -> sharedPreferences.edit().apply {
                putInt("win", win + 1).apply()
            }
            playerScore == scores[0].second -> sharedPreferences.edit().apply {
                putInt("draw", draw + 1).apply()
            }
            else -> sharedPreferences.edit().apply {
                putInt("lose", lose + 1).apply()
            }
        }
        var totalScore = 0
        for ((_, score) in gameStats.scores) {
            totalScore += score
        }
        val totalFish = sharedPreferences.getInt("totalFish", 0) + totalScore
        val totalMoves = sharedPreferences.getInt("totalMoves", 0) + gameStats.totalMoves
        sharedPreferences.edit().apply {
            putInt("totalFish", totalFish).apply()
            putInt("totalMoves", totalMoves).apply()
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
