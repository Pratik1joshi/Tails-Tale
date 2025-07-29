package com.example.tailstale

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {
    @Test
    fun `valid username and password`() {
        val result = LoginValidator.isValidLogin("validUser@gmail.com", "validPass123")
        assertTrue(result)
    }

    @Test
    fun `empty username`() {
        val result = LoginValidator.isValidLogin("", "validPass123")
        assertFalse(result)
    }

    @Test
    fun `password too short`() {
        val result = LoginValidator.isValidLogin("validUser@gmail.com", "123")
        assertFalse(result)
    }

    @Test
    fun `invalid gmail username`() {
        val result = LoginValidator.isValidLogin("invalidUser@yahoo.com", "validPass123")
        assertFalse(result)
    }
}

object LoginValidator {
    fun isValidLogin(username: String, password: String): Boolean {
        return isValidGmail(username) && password.length >= 6
    }

    private fun isValidGmail(username: String): Boolean {
        return username.isNotEmpty() && username.endsWith("@gmail.com")
    }
}