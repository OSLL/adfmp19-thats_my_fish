package osll.thatsmyfish.game

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_stats.*
import osll.thatsmyfish.R
import kotlin.math.max


class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences("stats", Context.MODE_PRIVATE)
        val win = sharedPreferences.getInt("win", 0)
        val draw = sharedPreferences.getInt("draw", 0)
        val lose = sharedPreferences.getInt("lose", 0)
        val total = win + draw + lose
        val totalFish = sharedPreferences.getInt("totalFish", 0)
        val avgFish = totalFish * 1.0 / max(total, 1)
        val totalMoves = sharedPreferences.getInt("totalMoves", 0)
        val avgMoves = totalMoves * 1.0 / max(total, 1)
        winTextView.text = win.toString()
        drawTextView.text = draw.toString()
        loseTextView.text = lose.toString()
        totalTextView.text = total.toString()
        avgFishTextView.text = "%.2f".format(avgFish)
        totalFishTextView.text = totalFish.toString()
        avgMovesTextView.text = "%.2f".format(avgMoves)
        totalMovesTextView.text = totalMoves.toString()
    }
}
