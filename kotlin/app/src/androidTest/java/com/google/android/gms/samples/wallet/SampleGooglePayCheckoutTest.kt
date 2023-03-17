package com.google.android.gms.samples.wallet

import android.util.Log
import android.view.View
import androidx.test.core.app.ActivityScenario.ActivityAction
import androidx.test.espresso.*
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.android.gms.samples.wallet.activity.CheckoutActivity
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


const val GOOGLE_PAY_SHEET_PACKAGE = "com.google.android.gms"

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SampleGooglePayCheckoutTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(CheckoutActivity::class.java)

    private val device: UiDevice

    init {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @Before
    fun setUp() {
    }

    @Test fun googlePayButtonIsPresent() {

        // Click on pay with Google Pay
        onView(withId(R.id.googlePayButton)).check(matches(isDisplayed()))
        onView(withId(R.id.googlePayButton)).perform(click())

        // Wait for payment sheet to come up
        device.waitForWindowUpdate(GOOGLE_PAY_SHEET_PACKAGE, 0);

        // Click on the card chooser
        // Some contentdescs:
        // - Select or add a payment method (not really working)
        // - Show list of payment methods. (arrow)
        val paymentMethodSelectorArrow = device.findObject(UiSelector()
            .className("android.widget.ImageView")
            .descriptionContains("Show list of payment methods."))
        paymentMethodSelectorArrow.click()

        // Change the card
        // TODO Consider testing lib created by eng, such that this test could be run without gmsCore installed
        val newSelectedCard = device.findObject(UiSelector()
            .className("android.widget.TextView")
            .textContains("0002"))
        newSelectedCard.click()

        // Confirm selection and back to the app
        val continueButton = device.findObject(UiSelector()
            .className("android.widget.Button")
            .text("Continue"))
        continueButton.click()

        // Wait Stripe's request is sent
        device.wait(
            Until.findObject(By.text("Payment completed successfully with Stripe")), 5000)
    }
}