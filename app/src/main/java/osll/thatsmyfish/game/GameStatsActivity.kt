package osll.thatsmyfish.game

import android.content.Intent
import android.os.Bundle
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
        mainMenuButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        playAgainButton.setOnClickListener {
            //TODO
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
