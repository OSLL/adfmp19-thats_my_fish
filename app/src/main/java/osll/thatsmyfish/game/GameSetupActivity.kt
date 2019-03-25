package osll.thatsmyfish.game

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game_setup.*
import osll.thatsmyfish.R
import osll.thatsmyfish.game.internal.GameType


class GameSetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_game_setup)

        gameTypeRadioGroup.check(R.id.singleGameRadioButton)
        playerCountRadioGroup.check(R.id.twoPlayersRadioButton)
        tileShapeRadioGroup.check(R.id.squareTileShapeRadioButton)

        runNewGameButton.setOnClickListener {

            val checkedGameTypeRadioButtonId = gameTypeRadioGroup.checkedRadioButtonId
            val gameType = when (checkedGameTypeRadioButtonId) {
                R.id.singleGameRadioButton -> GameType.SINGLE
                R.id.hotseatGameRadioButton -> GameType.HOTSEAT
                else -> {
                    throw IllegalArgumentException("Invalid radio button for game type")
                }
            }

            val checkedPlayerCountRadioButtonId = playerCountRadioGroup.checkedRadioButtonId
            val playerCount: Int = when (checkedPlayerCountRadioButtonId) {
                R.id.twoPlayersRadioButton -> 2
                R.id.threePlayersRadioButton -> 3
                R.id.fourPlayersRadioButton -> 4
                else -> {
                    throw IllegalArgumentException("Invalid radio button for players number")
                }
            }

            val botCount: Int
            val playerNames: ArrayList<String>
            when (gameType) {
                GameType.SINGLE -> {
                    botCount = playerCount - 1
                    playerNames = arrayListOf("You")
                }
                GameType.HOTSEAT -> {
                    botCount = 0
                    //TODO probably should use colors as names
                    playerNames = arrayOf("A", "B", "C", "D")
                            .sliceArray(0 until playerCount)
                            .toCollection(ArrayList())
                }
            }


            val checkedTileShapeRadioButtonId = tileShapeRadioGroup.checkedRadioButtonId
            val tileShape = when (checkedTileShapeRadioButtonId) {
                R.id.triangleTileShapeRadioButton -> "triangle"
                R.id.squareTileShapeRadioButton -> "square"
                R.id.hexagonTileShapeRadioButton -> "hexagon"
                else -> {
                    throw java.lang.IllegalArgumentException("Invalid radio button for tile shape")
                }
            }

            val activateShark = activateSharkCheckBox.isChecked
            val showFishCount = showFishCountCheckBox.isChecked

            val gameSettings = Bundle()
            gameSettings.apply {
                putString("gameType", gameType.toString())
                putInt("playerCount", playerCount)
                putInt("botCount", botCount)
                putStringArrayList("playerNames", playerNames)
                putString("tileShape", tileShape)
                putBoolean("activateShark", activateShark)
                putBoolean("showFishCount", showFishCount)
                //TODO probable should parametrize somehow
                putInt("fieldWidth", 6)
                putInt("fieldHeight", 6)
            }
            val result = Intent()
            result.putExtra("gameSettings", gameSettings)
            setResult(0, result)
            finish();
        }
    }

    override fun onBackPressed() {
        setResult(-1)
        finish()
    }
}
