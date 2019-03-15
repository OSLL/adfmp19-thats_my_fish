package osll.thatsmyfish.game

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game_stats.*
import osll.thatsmyfish.MainActivity
import osll.thatsmyfish.R

class GameStatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_stats)
        totalTimeTextView.text = toMinutesAndSeconds(intent.getLongExtra("totalTime", 0))
        totalMovesTextView.text = intent.getIntExtra("totalMoves", 0).toString()
        val playerNames = intent.getStringArrayListExtra("playerNames")
        val playerPoints = intent.getIntegerArrayListExtra("playerPoints")
        val playerNamesTableRow = TableRow(this)
        for (playerName in playerNames) {
            val playerNameTextView = TextView(this)
            playerNameTextView.gravity = Gravity.CENTER
            playerNameTextView.text = playerName
            playerNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            playerNamesTableRow.addView(playerNameTextView)
        }
        resultsTableLayout.addView(playerNamesTableRow)
        val playerPointsTableRow = TableRow(this)
        for (playerPoint in playerPoints) {
            val playerPointTextView = TextView(this)
            playerPointTextView.gravity = Gravity.CENTER
            playerPointTextView.text = playerPoint.toString()
            playerPointTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            playerPointsTableRow.addView(playerPointTextView)
        }
        resultsTableLayout.addView(playerPointsTableRow)

        mainMenuButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        playAgainButton.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java).apply {
                putExtras(intent.extras!!)
            })
        }
    }

    private fun toMinutesAndSeconds(millis: Long): String {
        val totalSeconds = millis / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60
        return if (seconds < 10) {
            "$minutes:0$seconds"
        } else {
            "$minutes:$seconds"
        }
    }
}
