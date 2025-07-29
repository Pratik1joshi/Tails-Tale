package com.example.tailstale

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignupValidatorTest {
    @Test
    fun `valid email`() {
        val result = SignupValidator.isValidSignup("validUser@gmail.com", "password123", "password123", "John Doe")
        assertTrue(result)
    }

    @Test
    fun `password does not match confirmation`() {
        val result = SignupValidator.isValidSignup("validUser@gmail.com", "password123", "password321", "John Doe")
        assertFalse(result)
    }

    @Test
    fun `name is empty`() {
        val result = SignupValidator.isValidSignup("validUser@gmail.com", "password123", "password123", "")
        assertFalse(result)
    }
}

object SignupValidator {
    fun isValidSignup(email: String, password: String, confirmPassword: String, name: String): Boolean {
        return isValidEmail(email) && password == confirmPassword && name.isNotEmpty()
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.endsWith(".com")
    }
}