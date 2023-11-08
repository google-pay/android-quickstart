package com.google.android.gms.samples.wallet

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import com.google.android.gms.samples.wallet.activity.CheckoutActivity
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

    @get:Rule
    val composeTestRule = createAndroidComposeRule<CheckoutActivity>()

    private val device: UiDevice

    init {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @Before
    fun setUp() {
    }

    @Test
    fun testDummyVisaCardPayment() {

        // [Compose] Click on pay with Google Pay
        composeTestRule.onNodeWithTag("payButton").performClick()
        composeTestRule.waitForIdle()

        // [UIAutomator] Wait for payment sheet to come up
        device.waitForWindowUpdate(GOOGLE_PAY_SHEET_PACKAGE, 0)

        // [UIAutomator] Click on the card chooser
        val paymentMethodSelectorArrow = device.findObject(
            UiSelector()
                .className("android.widget.ImageView")
                .descriptionContains("Show list of payment methods")
        )
        paymentMethodSelectorArrow.click()

        // [UIAutomator] Change the card
        val targetCardSelector = UiSelector()
            .className("android.widget.TextView")
            .textContains("Visa")

        val cardList = UiScrollable(UiSelector().className("android.widget.ScrollView"))
        cardList.scrollIntoView(targetCardSelector)
        device.findObject(targetCardSelector).click()

        // [UIAutomator] Confirm selection and back to the app
        val continueButton = device.findObject(
            UiSelector()
                .className("android.widget.Button")
                .text("Continue")
        )
        continueButton.click()

        // [Compose] Confirm that the success screen is visible
        composeTestRule.onNodeWithTag("successScreen").assertIsDisplayed()
    }
}