package student.projects.fkj_consultants_app

import org.junit.Assert.*
import org.junit.Test

class ForgotPasswordActivityUnitTest {

    // Regex-based email validation for JVM tests
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return emailRegex.matches(email)
    }

    @Test
    fun validEmail_ReturnsTrue() {
        val email = "test@example.com"
        assertTrue(isValidEmail(email))
    }

    @Test
    fun invalidEmail_ReturnsFalse() {
        val email = "invalid-email"
        assertFalse(isValidEmail(email))
    }

    @Test
    fun emptyEmail_ReturnsFalse() {
        val email = ""
        assertFalse(isValidEmail(email))
    }
}
