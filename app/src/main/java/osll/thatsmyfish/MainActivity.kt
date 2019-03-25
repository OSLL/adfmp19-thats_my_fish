package osll.thatsmyfish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import osll.thatsmyfish.game.GameActivity
import osll.thatsmyfish.game.GameSetupActivity
import osll.thatsmyfish.game.GameStatsActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        startNewGameButton.setOnClickListener {
            startActivityForResult(Intent(this, GameSetupActivity::class.java),
                    GAME_SETTINGS_REQUEST_ID)
        }
        statsButton.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GAME_SETTINGS_REQUEST_ID && resultCode == 0) {
            val settings = data?.getBundleExtra("gameSettings")
            if (settings != null) {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("gameSettings", settings)
                startActivityForResult(intent, RUN_GAME_REQUEST_ID)
            }
        }
        if (requestCode == RUN_GAME_REQUEST_ID && resultCode == 0) {
            val settings = data?.getBundleExtra("gameSettings")
            val results = data?.getBundleExtra("gameResults")
            if (data != null && results != null) {
                val intent = Intent(this, GameStatsActivity::class.java)
                intent.putExtra("gameSettings", settings)
                intent.putExtra("gameResults", results)
                startActivityForResult(intent, SHOW_GAME_RESULTS_REQUEST_ID)
            }
        }
        if (requestCode == SHOW_GAME_RESULTS_REQUEST_ID && resultCode == 1) {
            val settings = data?.getBundleExtra("gameSettings")
            if (settings != null) {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("gameSettings", settings)
                startActivityForResult(intent, RUN_GAME_REQUEST_ID)
            }
        }
    }

    companion object {
        const val GAME_SETTINGS_REQUEST_ID = 1
        const val RUN_GAME_REQUEST_ID = 2
        const val SHOW_GAME_RESULTS_REQUEST_ID = 3
    }
}

class IllegalIntentReceived(resource: String, context: Context) : Exception(
        "Resource $resource not found in intent at ${context.javaClass.simpleName}"
)
