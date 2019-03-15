package osll.thatsmyfish

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import osll.thatsmyfish.game.GameSetupActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startNewGameButton.setOnClickListener {
            startActivity(Intent(this, GameSetupActivity::class.java))
        }
        statsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }
}
