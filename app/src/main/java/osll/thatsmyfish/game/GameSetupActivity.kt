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
        setContentView(R.layout.activity_game_setup)

        gameTypeRadioGroup.check(R.id.singleGameRadioButton)
        playerCountRadioGroup.check(R.id.twoPlayersRadioButton)
        tileShapeRadioGroup.check(R.id.triangleTileShapeRadioButton)

        startNewGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)

            val checkedGameTypeRadioButtonId = gameTypeRadioGroup.checkedRadioButtonId
            val gameType = when (checkedGameTypeRadioButtonId) {
                R.id.singleGameRadioButton -> GameType.SINGLE
                R.id.hotseatGameRadioButton -> GameType.HOTSEAT
                else -> {
                    throw IllegalArgumentException("Invalid radio button for game type")
                }
            }
            intent.apply {
                putExtra("gameType", gameType.toString())
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
            intent.apply {
                putExtra("playerCount", playerCount.toString())
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
                            .sliceArray(0..playerCount)
                            .toCollection(ArrayList())
                }
            }
            intent.apply {
                putExtra("botCount", botCount)
                putStringArrayListExtra("playerNames", playerNames)
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
            intent.apply {
                putExtra("tileShape", tileShape.toString())
            }

            //TODO probable should parametrize somehow
            val fieldWidth = 6
            val fieldHeight = 6
            intent.apply {
                putExtra("fieldWidth", fieldWidth)
                putExtra("fieldHeight", fieldHeight)
            }

            val activateShark = activateSharkCheckBox.isChecked
            intent.apply {
                putExtra("activateShark", activateShark)
            }
            val showFishCount = showFishCountCheckBox.isChecked
            intent.apply {
                putExtra("showFishCount", showFishCount)
            }

            startActivity(intent)
        }
    }
}
