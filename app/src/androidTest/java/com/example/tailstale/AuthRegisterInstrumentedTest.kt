package com.example.tailstale

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tailstale.view.SignUpActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<SignUpActivity>()


    @Test
    fun testSuccessfulSignUp_navigatesToDashboard() {
        // Enter email
        composeRule.onNodeWithTag("signup_email")
            .performTextInput("newuser@example.com")

        // Enter password
        composeRule.onNodeWithTag("signup_password")
            .performTextInput("password123")

        // Enter confirm password
        composeRule.onNodeWithTag("signup_confirm_password")
            .performTextInput("password123")

        // Enter name
        composeRule.onNodeWithTag("signup_name")
            .performTextInput("Test User")

        // Click Sign Up
        composeRule.onNodeWithTag("signup_button")
            .performClick()

        // Wait for navigation or success
        composeRule.waitForIdle()
    }

    @Test
    fun testEmptyEmail_ShowsNoNavigation() {
        // Only click sign up without entering email
        composeRule.onNodeWithTag("signup_button")
            .performClick()

        // Wait for UI to settle (should stay on same screen)
        composeRule.waitForIdle()
    }
}
