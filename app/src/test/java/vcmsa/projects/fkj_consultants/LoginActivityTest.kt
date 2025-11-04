package vcmsa.projects.fkj_consultants.activities

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for LoginActivity validation logic and helper methods
 * Fully JVM compatible (no Android dependencies)
 */
class LoginActivityTest {

    private lateinit var loginValidator: LoginValidator

    @Before
    fun setup() {
        loginValidator = LoginValidator()
    }

    // ========== Email Validation Tests ==========
    @Test
    fun `validateEmail returns error when email is empty`() {
        val result = loginValidator.validateEmail("")
        assertFalse(result.isValid)
        assertEquals("Email is required", result.errorMessage)
    }

    @Test
    fun `validateEmail returns error when email is blank`() {
        val result = loginValidator.validateEmail("   ")
        assertFalse(result.isValid)
        assertEquals("Email is required", result.errorMessage)
    }

    @Test
    fun `validateEmail returns error when email format is invalid`() {
        val result = loginValidator.validateEmail("invalid-email")
        assertFalse(result.isValid)
        assertEquals("Enter a valid email", result.errorMessage)
    }

    @Test
    fun `validateEmail returns error when email missing at symbol`() {
        val result = loginValidator.validateEmail("testexample.com")
        assertFalse(result.isValid)
        assertEquals("Enter a valid email", result.errorMessage)
    }

    @Test
    fun `validateEmail returns error when email missing domain`() {
        val result = loginValidator.validateEmail("test@")
        assertFalse(result.isValid)
        assertEquals("Enter a valid email", result.errorMessage)
    }

    @Test
    fun `validateEmail returns success for valid email`() {
        val result = loginValidator.validateEmail("test@example.com")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validateEmail trims whitespace from email`() {
        val result = loginValidator.validateEmail("  test@example.com  ")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validateEmail accepts email with plus sign`() {
        val result = loginValidator.validateEmail("test+alias@example.com")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validateEmail accepts email with subdomain`() {
        val result = loginValidator.validateEmail("test@mail.example.com")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // ========== Password Validation Tests ==========
    @Test
    fun `validatePassword returns error when password is empty`() {
        val result = loginValidator.validatePassword("")
        assertFalse(result.isValid)
        assertEquals("Password is required", result.errorMessage)
    }

    @Test
    fun `validatePassword returns error when password is blank`() {
        val result = loginValidator.validatePassword("   ")
        assertFalse(result.isValid)
        assertEquals("Password is required", result.errorMessage)
    }

    @Test
    fun `validatePassword returns error when password is less than 6 characters`() {
        val result = loginValidator.validatePassword("12345")
        assertFalse(result.isValid)
        assertEquals("Password must be at least 6 characters", result.errorMessage)
    }

    @Test
    fun `validatePassword returns success for password with exactly 6 characters`() {
        val result = loginValidator.validatePassword("123456")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validatePassword returns success for password longer than 6 characters`() {
        val result = loginValidator.validatePassword("password123")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    @Test
    fun `validatePassword trims whitespace before validation`() {
        val result = loginValidator.validatePassword("  pass123  ")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // ========== Combined Validation Tests ==========
    @Test
    fun `validateCredentials returns error when both fields are empty`() {
        val result = loginValidator.validateCredentials("", "")
        assertFalse(result.isValid)
        assertEquals("Email is required", result.errorMessage)
    }

    @Test
    fun `validateCredentials returns error when email is invalid`() {
        val result = loginValidator.validateCredentials("invalid-email", "password123")
        assertFalse(result.isValid)
        assertEquals("Enter a valid email", result.errorMessage)
    }

    @Test
    fun `validateCredentials returns error when password is too short`() {
        val result = loginValidator.validateCredentials("test@example.com", "12345")
        assertFalse(result.isValid)
        assertEquals("Password must be at least 6 characters", result.errorMessage)
    }

    @Test
    fun `validateCredentials returns success for valid credentials`() {
        val result = loginValidator.validateCredentials("test@example.com", "password123")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // ========== Admin Role Tests ==========
    @Test
    fun `isAdminEmail returns true for kush gmail`() {
        assertTrue(LoginValidator.isAdminEmail("kush@gmail.com"))
    }

    @Test
    fun `isAdminEmail returns true for keitumetse01 gmail`() {
        assertTrue(LoginValidator.isAdminEmail("keitumetse01@gmail.com"))
    }

    @Test
    fun `isAdminEmail returns true for malikaOlivia gmail`() {
        assertTrue(LoginValidator.isAdminEmail("malikaOlivia@gmail.com"))
    }

    @Test
    fun `isAdminEmail returns true for JamesJameson gmail`() {
        assertTrue(LoginValidator.isAdminEmail("JamesJameson@gmail.com"))
    }

    @Test
    fun `isAdminEmail is case insensitive for uppercase`() {
        assertTrue(LoginValidator.isAdminEmail("KUSH@GMAIL.COM"))
    }

    @Test
    fun `isAdminEmail is case insensitive for mixed case`() {
        assertTrue(LoginValidator.isAdminEmail("Kush@Gmail.Com"))
    }

    @Test
    fun `isAdminEmail returns false for non-admin email`() {
        assertFalse(LoginValidator.isAdminEmail("regular@user.com"))
    }

    @Test
    fun `isAdminEmail returns false for empty string`() {
        assertFalse(LoginValidator.isAdminEmail(""))
    }

    @Test
    fun `isAdminEmail returns false for null`() {
        assertFalse(LoginValidator.isAdminEmail(null))
    }

    @Test
    fun `admin emails list has correct count`() {
        assertEquals(4, LoginValidator.ADMIN_EMAILS.size)
    }

    // ========== Error Message Formatting Tests ==========
    @Test
    fun `formatLoginError returns correct message for no user record`() {
        val message = "no user record corresponding to this identifier"
        val result = LoginValidator.formatLoginError(message)
        assertEquals("No account found with this email", result)
    }

    @Test
    fun `formatLoginError returns correct message for invalid password`() {
        val message = "The password is invalid"
        val result = LoginValidator.formatLoginError(message)
        assertEquals("Incorrect password", result)
    }

    @Test
    fun `formatLoginError returns correct message for network error`() {
        val message = "A network error occurred"
        val result = LoginValidator.formatLoginError(message)
        assertEquals("Network error. Check your connection", result)
    }

    @Test
    fun `formatLoginError returns generic message for unknown error`() {
        val message = "Some random error occurred"
        val result = LoginValidator.formatLoginError(message)
        assertEquals("Login failed: Some random error occurred", result)
    }

    @Test
    fun `formatLoginError returns generic message for null error`() {
        val result = LoginValidator.formatLoginError(null)
        assertEquals("Login failed: Unknown error", result)
    }

    @Test
    fun `formatLoginError is case insensitive for error matching`() {
        val message = "NO USER RECORD FOUND"
        val result = LoginValidator.formatLoginError(message)
        assertEquals("No account found with this email", result)
    }
}

/**
 * Validator class containing business logic for login validation
 * Fully JVM compatible (no Android dependencies)
 */
class LoginValidator {

    companion object {
        val ADMIN_EMAILS = listOf(
            "kush@gmail.com",
            "keitumetse01@gmail.com",
            "malikaOlivia@gmail.com",
            "JamesJameson@gmail.com"
        )

        fun isAdminEmail(email: String?): Boolean {
            if (email.isNullOrEmpty()) return false
            return ADMIN_EMAILS.any { it.equals(email, ignoreCase = true) }
        }

        fun formatLoginError(message: String?): String {
            return when {
                message?.contains("no user record", ignoreCase = true) == true ->
                    "No account found with this email"
                message?.contains("password is invalid", ignoreCase = true) == true ->
                    "Incorrect password"
                message?.contains("network error", ignoreCase = true) == true ->
                    "Network error. Check your connection"
                else -> "Login failed: ${message ?: "Unknown error"}"
            }
        }
    }

    // Pure Kotlin email regex
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Email is required")
            !EMAIL_REGEX.matches(trimmed) -> ValidationResult(false, "Enter a valid email")
            else -> ValidationResult(true, null)
        }
    }

    fun validatePassword(password: String): ValidationResult {
        val trimmed = password.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Password is required")
            trimmed.length < 6 -> ValidationResult(false, "Password must be at least 6 characters")
            else -> ValidationResult(true, null)
        }
    }

    fun validateCredentials(email: String, password: String): ValidationResult {
        val emailResult = validateEmail(email)
        if (!emailResult.isValid) return emailResult
        return validatePassword(password)
    }
}
//(Gideon, 2023).

