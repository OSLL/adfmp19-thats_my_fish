package osll.thatsmyfish.game

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.text.TextPaint
import android.util.Size
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import osll.thatsmyfish.IllegalIntentReceived
import osll.thatsmyfish.R
import osll.thatsmyfish.game.internal.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class GameActivity : AppCompatActivity() {
    private lateinit var gameView: GameFieldView

    private val scoresView
        get() = findViewById<LinearLayout>(R.id.game_scores)
    private lateinit var playerViews: Map<AbstractPlayer, PlayerView>
    private var gameType: GameType = GameType.SINGLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_game)

        val settings = intent.getBundleExtra("gameSettings")

        val playerNames = settings.getStringArrayList("playerNames")
                ?: throw IllegalIntentReceived("playerNames", this)
        val bots = settings.getInt("botCount", 0)
        val fieldSize = Size(
                settings.getInt("fieldWidth", 5),
                settings.getInt("fieldHeight", 5)
        )
        val chosenShape = settings.getString("tileShape")
                ?: throw IllegalIntentReceived("tileShape", this)
        val gameType = settings.getString("gameType")
                ?: throw IllegalIntentReceived("gameType", this)
        this.gameType = GameType.valueOf(gameType)

        val playerColors: Array<Int> = resources.getIntArray(R.array.player_colors).toTypedArray()
        val humanPlayers = playerNames.zip(playerColors).map { Player(it.first, it.second) }
        val botPlayers = List(bots) { "Bot #$it" }.zip(playerColors.drop(playerNames.size)).map {
            Bot(it.first, it.second)
        }
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
        gameView = GameFieldView(this, gameHandler.tiles,
                gameHandler.shape, gameHandler.size)
        gameHolder.addView(gameView)
        gameHandler.gameWatcher = object : GameHandler.GameWatcher {
            override fun playerWait(player: AbstractPlayer) {}

            override fun scoreUpdate(scores: Map<AbstractPlayer, Int>) {
                scores.map {playerViews[it.key]!!.postInvalidate()}
            }

            override fun gameFinished(gameStatistics: GameStatistics) {
                if (this@GameActivity.gameType == GameType.SINGLE) {
                    recalculateStats(gameStatistics)
                }
                val gameScores = gameStatistics.scores
                val sortedPlayerNames = gameScores.map { it.first.name }
                val playerPoints = gameScores.map { it.second }

                val gameResults = Bundle().apply {
                    putLong("totalTime", gameStatistics.time)
                    putInt("totalMoves", gameStatistics.totalMoves)
                    putStringArrayList("sortedPlayerNames", sortedPlayerNames.toCollection(ArrayList()))
                    putIntegerArrayList("playerPoints", playerPoints.toCollection(ArrayList()))
                }
                val intent = Intent().apply {
                    putExtra("gameResults", gameResults)
                    putExtra("gameSettings", intent.getBundleExtra("gameSettings"))
                }
                setResult(0, intent)
                finish()
            }
        }
        gameHandler.start(gameView)
    }


    private fun recalculateStats(gameStats: GameStatistics) {
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
