package com.example.tailstale

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tailstale.repo.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AuthRegisterInstrumentedTest {

    @Test
    fun testRegister_Instrumented() {
        val email = "newuser@example.com"
        val password = "newPassword"
        val latch = CountDownLatch(1)
        var expectedResult = "Initial Value"

        val auth = FirebaseAuth.getInstance()
        val authRepository = AuthRepositoryImpl(auth)

        val callback = { success: Boolean, message: String? ->
            expectedResult = message ?: "Callback message is null"
            latch.countDown()
        }

        authRepository.register(email, password, callback)

        latch.await()

        assertEquals("Registration successfully", expectedResult)
    }
}
