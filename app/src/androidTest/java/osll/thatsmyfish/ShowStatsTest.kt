package osll.thatsmyfish

import org.junit.Assert.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import kotlinx.android.synthetic.main.activity_main.*
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class ShowStatsTest {

    @get:Rule
    var mActivityRule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun showStatsAndBackToMenu() {
        onView(withId(R.id.statsButton)).check(matches(isDisplayed()))
        onView(withId(R.id.statsButton)).perform(click())
        onView(withId(R.id.winConstantTextView)).check(matches(isDisplayed()))
        onView(withId(R.id.winConstantTextView)).check(matches(withText("Win")))
        onView(withId(R.id.mainMenuButton)).check(matches(isDisplayed()))
        onView(withId(R.id.mainMenuButton)).perform(click())
        onView(withId(R.id.statsButton)).check(matches(isDisplayed()))
    }

}