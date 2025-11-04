package vcmsa.projects.fkj_consultants.activities

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

// =====================
// Unit Tests
// =====================

class RegisterActivityTest {

    private lateinit var registerValidator: RegisterValidator

    @Before
    fun setup() {
        registerValidator = RegisterValidator()
    }

    // ========== First Name Tests ==========
    @Test
    fun `validateFirstName returns error when first name is empty`() {
        val result = registerValidator.validateFirstName("")
        assertFalse(result.isValid)
        assertEquals("First name is required", result.errorMessage)
    }

    @Test
    fun `validateFirstName trims whitespace`() {
        val result = registerValidator.validateFirstName("  John  ")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // ========== Last Name Tests ==========
    @Test
    fun `validateLastName returns error when last name is empty`() {
        val result = registerValidator.validateLastName("")
        assertFalse(result.isValid)
        assertEquals("Last name is required", result.errorMessage)
    }

    // ========== Email Tests ==========
    @Test
    fun `validateEmail returns error when email format is invalid`() {
        val result = registerValidator.validateEmail("invalid-email")
        assertFalse(result.isValid)
        assertEquals("Enter a valid email", result.errorMessage)
    }

    @Test
    fun `validateEmail returns success for valid email`() {
        val result = registerValidator.validateEmail("test@example.com")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // ========== Phone Tests ==========
    @Test
    fun `validatePhone returns error for invalid phone`() {
        val result = registerValidator.validatePhone("abc")
        assertFalse(result.isValid)
        assertEquals("Enter a valid phone number", result.errorMessage)
    }

    @Test
    fun `validatePhone returns success for valid phone`() {
        val result = registerValidator.validatePhone("+27 123 456 7890")
        assertTrue(result.isValid)
    }

    // ========== Password Tests ==========
    @Test
    fun `validatePassword returns error for short password`() {
        val result = registerValidator.validatePassword("123")
        assertFalse(result.isValid)
        assertEquals("Password must be at least 6 characters", result.errorMessage)
    }

    @Test
    fun `validatePassword returns success for valid password`() {
        val result = registerValidator.validatePassword("password123")
        assertTrue(result.isValid)
    }

    // ========== Password Match Tests ==========
    @Test
    fun `validatePasswordMatch returns error when passwords do not match`() {
        val result = registerValidator.validatePasswordMatch("pass1", "pass2")
        assertFalse(result.isValid)
        assertEquals("Passwords do not match", result.errorMessage)
    }

    @Test
    fun `validatePasswordMatch returns success when passwords match`() {
        val result = registerValidator.validatePasswordMatch("pass123", "pass123")
        assertTrue(result.isValid)
    }

    // ========== Registration Tests ==========
    @Test
    fun `validateRegistration returns success for valid registration`() {
        val data = RegistrationData(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "+27123456789",
            password = "password123",
            confirmPassword = "password123"
        )
        val result = registerValidator.validateRegistration(data)
        assertTrue(result.isValid)
    }
}

// =====================
// Validator Class
// =====================

class RegisterValidator {

    companion object {
        fun createUserData(
            uid: String,
            firstName: String,
            lastName: String,
            email: String,
            phone: String
        ): Map<String, Any> {
            val currentTime = System.currentTimeMillis()
            return mapOf(
                "uid" to uid,
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phoneNumber" to phone,
                "role" to "user",
                "createdAt" to currentTime,
                "lastLogin" to currentTime
            )
        }
    }

    fun validateFirstName(firstName: String): ValidationResult {
        val trimmed = firstName.trim()
        return if (trimmed.isEmpty()) ValidationResult(false, "First name is required")
        else ValidationResult(true, null)
    }

    fun validateLastName(lastName: String): ValidationResult {
        val trimmed = lastName.trim()
        return if (trimmed.isEmpty()) ValidationResult(false, "Last name is required")
        else ValidationResult(true, null)
    }

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Email is required")
            !regex.matches(trimmed) -> ValidationResult(false, "Enter a valid email")
            else -> ValidationResult(true, null)
        }
    }

    fun validatePhone(phone: String): ValidationResult {
        val trimmed = phone.trim()
        val regex = "^\\+?[0-9\\- ]{7,15}\$".toRegex()
        return when {
            trimmed.isEmpty() -> ValidationResult(false, "Phone number is required")
            !regex.matches(trimmed) -> ValidationResult(false, "Enter a valid phone number")
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

    fun validatePasswordMatch(password: String, confirmPassword: String): ValidationResult {
        return if (password == confirmPassword) ValidationResult(true, null)
        else ValidationResult(false, "Passwords do not match")
    }

    fun validateRegistration(data: RegistrationData): ValidationResult {
        validateFirstName(data.firstName).let { if (!it.isValid) return it }
        validateLastName(data.lastName).let { if (!it.isValid) return it }
        validateEmail(data.email).let { if (!it.isValid) return it }
        validatePhone(data.phone).let { if (!it.isValid) return it }
        validatePassword(data.password).let { if (!it.isValid) return it }
        return validatePasswordMatch(data.password, data.confirmPassword)
    }
}

// =====================
// Data Classes
// =====================

data class RegistrationData(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)

data class ValidationResult(val isValid: Boolean, val errorMessage: String?)
//Gideon, O. O., 2023. Introduction to Unit Testing in Android Kotlin. [Online]
//Available at: https://medium.com/@deonolarewaju/introduction-to-unit-testing-in-android-kotlin-4331eb2366a9
//[Accessed 25 October 2025].