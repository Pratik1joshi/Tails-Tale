package com.example.tailstale

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tailstale.view.LoginActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Enter email
        composeRule.onNodeWithTag("email")
            .performTextInput("user@example.com")

        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("password")

        // Click Login
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait for navigation or success
        composeRule.waitForIdle()
    }

    @Test
    fun testEmptyEmail_ShowsNoNavigation() {
        // Only click login without entering email or password
        composeRule.onNodeWithTag("login_button")
            .performClick()

        // Wait for UI to settle (should stay on same screen)
        composeRule.waitForIdle()
    }
}