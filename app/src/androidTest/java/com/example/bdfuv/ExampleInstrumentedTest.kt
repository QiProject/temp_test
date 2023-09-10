package com.example.bdfuv

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ScrollView
import androidx.core.view.GravityCompat
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleInstrumentedTest {
    //    @Test
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getTargetContext()
//        assertEquals("com.example.iciengineer", appContext.packageName)
//    }
    @get:Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)


    @Test
    fun clickOnYourNavigationItem_ShowsYourScreen() {
        // Open Drawer to click on navigation.
        openDrawer()

        goToSettingPage()

        assertSettingScreen()
    }

    //    @Test
    fun testSettingPageCorrectCode() {
        clickOnYourNavigationItem_ShowsYourScreen()

        // Type text and then press the button.
        onView(withId(R.id.codeEditText)).perform(
            typeText("4223"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.okButton)).perform(click())

        // This view is in a different Activity, no need to tell Espresso.
        onView(ViewMatchers.withText("EXIT"))
            .check(matches(isDisplayed()))

    }

    @Test
    fun testSettingPageWrongCode() {
        clickOnYourNavigationItem_ShowsYourScreen()

        // Type text and then press the button.
        onView(withId(R.id.codeEditText)).perform(
            typeText("0000"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.okButton)).perform(click())

        onView(withText("invalid code")).inRoot(withDecorView(not(activityTestRule.getActivity().getWindow().getDecorView())))
            .check(matches(isDisplayed()))
    }


    fun goToSettingPage() {
        onView(withId(R.id.nav_view))
            .perform(NavigationViewActions.navigateTo(R.id.settingFragment));
    }

    private fun assertSettingScreen() {
        onView(ViewMatchers.withText("code"))
            .check(matches(isDisplayed()))
    }


    fun openDrawer() {
        onView(withId(R.id.drawer_layout))
            .check(matches(isDisplayed())) // Left Drawer should be closed.
            .perform(open(GravityCompat.START)).perform()
    }


    @Test
    fun settingPageNotConnected() {
        testSettingPageCorrectCode()

        val appCompatButton3 = onView(
            Matchers.allOf(
                withId(R.id.deviceConfigButton), withText("Device Configuration"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.main_fragment),
                        0
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatButton3.perform(click())


        onView(withText("Please connect to device first"))
        .inRoot(isDialog()) // <---
            .check(matches(isDisplayed()))

//        val textView2 = onView(
//            Matchers.allOf(
//                withId(android.R.id.message), withText("Please connect to device first"),
//                childAtPosition(
//                    childAtPosition(
//                        IsInstanceOf.instanceOf(ScrollView::class.java),
//                        0
//                    ),
//                    0
//                ),
//                isDisplayed()
//            )
//        )
//        textView2.check(matches(withText("Please connect to device first")))
    }


    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

}
