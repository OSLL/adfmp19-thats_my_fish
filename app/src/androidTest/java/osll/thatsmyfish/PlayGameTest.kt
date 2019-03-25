package osll.thatsmyfish

import android.util.Log
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import osll.thatsmyfish.game.GameFieldView

class PlayGameTest {

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun showGameCreateActivityAndGoBack() {
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())


        onView(withId(R.id.threePlayersRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))
        onView(withId(R.id.twoPlayersRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))
        onView(withId(R.id.threePlayersRadioButton))
                .check(matches(not(isChecked())))

        onView(isRoot())
                .perform(ViewActions.pressBack())
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
    }


    @Test
    fun showGameActivityAndGoBack() {
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(withId(R.id.runNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(withId(R.id.game_scores))
                .check(matches(isDisplayed()))

        onView(isRoot())
                .perform(ViewActions.pressBack())
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
    }

    @Test
    fun playGameAndGoToMenu() {
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        // setup game
        onView(withId(R.id.twoPlayersRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.hotseatGameRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.squareTileShapeRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.runNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(instanceOf(GameFieldView::class.java)).perform(PlayGame)

        onView(withId(R.id.resultsConstantTextView))
                .check(matches(isDisplayed()))
        onView(withId(R.id.mainMenuButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
    }

    @Test
    fun playGameAndGoBack() {
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        // setup game
        onView(withId(R.id.twoPlayersRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.hotseatGameRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.squareTileShapeRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.runNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(instanceOf(GameFieldView::class.java)).perform(PlayGame)

        onView(withId(R.id.resultsConstantTextView))
                .check(matches(isDisplayed()))

        onView(isRoot())
                .perform(ViewActions.pressBack())
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
    }

    @Test
    fun playGameAndPlayAgain() {
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        // setup game
        onView(withId(R.id.twoPlayersRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.hotseatGameRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.squareTileShapeRadioButton))
                .check(matches(isDisplayed()))
                .perform(click())
                .check(matches(isChecked()))

        onView(withId(R.id.runNewGameButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(instanceOf(GameFieldView::class.java)).perform(PlayGame)

        onView(withId(R.id.resultsConstantTextView))
                .check(matches(isDisplayed()))

        onView(withId(R.id.playAgainButton))
                .check(matches(isDisplayed()))
                .perform(click())

        onView(instanceOf(GameFieldView::class.java)).perform(PlayGame)

        onView(withId(R.id.mainMenuButton))
                .check(matches(isDisplayed()))
                .perform(click())
        onView(withId(R.id.startNewGameButton))
                .check(matches(isDisplayed()))
    }

    object PlayGame : ViewAction {
        override fun getDescription(): String = "Play game"

        override fun getConstraints(): Matcher<View> = instanceOf(GameFieldView::class.java)

        override fun perform(uiController: UiController?, view: View?) {
            val field = view as GameFieldView
            Log.e("MY_OWN_TAG", field.game.toString())
            // put penguins
            for (j in 0 until 6) {
                // clicks via .perform(click()) doesn't work here - it hangs up
                getCell(field, 0, j).callOnClick()
                Thread.sleep(100);
            }
            // move penguins first time
            for (j in 0 until 6) {
                getCell(field,0, j).callOnClick()
                Thread.sleep(100);
                getCell(field, 2, j).callOnClick()
                Thread.sleep(100);
            }
            // move penguins first time
            for (j in 0 until 6) {
                getCell(field,2, j).callOnClick()
                Thread.sleep(100);
                getCell(field,1, j).callOnClick()
                Thread.sleep(100);
            }
        }

        private fun getCell(gameView: GameFieldView, i: Int, j: Int): View {
            return gameView.getChildAt(i + j * 6)
        }
    }
}