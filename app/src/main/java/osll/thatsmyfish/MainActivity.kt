package osll.thatsmyfish

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import osll.thatsmyfish.game.GameActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.example_game_button).setOnClickListener {
            startActivity(Intent(
                    this, GameActivity::class.java
            ).apply {
                putExtra("players", arrayOf("Shurik"))
                putExtra("botCount", 1)
                putExtra("fieldWidth", 6)
                putExtra("fieldHeight", 6)
                putExtra("tileShape", "triangle")
            })
        }
    }
}
