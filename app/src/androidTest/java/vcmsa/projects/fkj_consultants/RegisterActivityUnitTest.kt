// RegisterActivityUnitTest.kt
package student.projects.fkj_consultants_app

import org.junit.Assert.*
import org.junit.Test

class RegisterActivityUnitTest {

    // Utility function to validate password rules
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
        return password.matches(passwordRegex)
    }

    // Utility function to check if passwords match
    private fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    // Utility function to check if all fields are filled
    private fun areFieldsFilled(vararg fields: String): Boolean {
        return fields.all { it.isNotEmpty() }
    }

    // ======= TEST CASES =======

    @Test
    fun allFieldsFilled_ReturnsTrue() {
        val result = areFieldsFilled("John", "Doe", "jdoe", "john@example.com", "123 Street", "Password1", "Password1")
        assertTrue(result)
    }

    @Test
    fun missingFields_ReturnsFalse() {
        val result = areFieldsFilled("John", "", "jdoe")
        assertFalse(result)
    }

    @Test
    fun passwordValid_ReturnsTrue() {
        assertTrue(isValidPassword("Password1"))
    }

    @Test
    fun passwordInvalid_ReturnsFalse() {
        assertFalse(isValidPassword("pass")) // Too short, no number
        assertFalse(isValidPassword("password")) // No number
        assertFalse(isValidPassword("12345678")) // No letter
    }

    @Test
    fun passwordsMatch_ReturnsTrue() {
        assertTrue(doPasswordsMatch("Password1", "Password1"))
    }

    @Test
    fun passwordsDoNotMatch_ReturnsFalse() {
        assertFalse(doPasswordsMatch("Password1", "Password2"))
    }
}
