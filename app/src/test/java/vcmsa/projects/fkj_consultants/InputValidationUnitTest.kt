package vcmsa.projects.fkj_consultants

import org.junit.Assert.*
import org.junit.Test

class InputValidationUnitTest {

    // ----- Registration Tests -----

    @Test
    fun `register fails when fields are empty`() {
        val firstName = ""
        val lastName = ""
        val email = ""
        val password = ""
        val confirmPassword = ""

        val isValid = firstName.isNotEmpty()
                && lastName.isNotEmpty()
                && email.isNotEmpty()
                && password.isNotEmpty()
                && confirmPassword.isNotEmpty()

        assertFalse("Registration should fail with empty fields", isValid)
    }

    @Test
    fun `register fails when passwords do not match`() {
        val password = "123456"
        val confirmPassword = "654321"

        val isValid = password == confirmPassword
        assertFalse("Registration should fail if passwords don't match", isValid)
    }

    @Test
    fun `register fails when password is too short`() {
        val password = "123"
        val isValid = password.length >= 6

        assertFalse("Registration should fail if password is too short", isValid)
    }

    @Test
    fun `register succeeds with valid inputs`() {
        val firstName = "Mavuso"
        val lastName = "Mavuvu"
        val email = "Mavuso@gmail.com"
        val password = "123456"
        val confirmPassword = "123456"

        val isValid = firstName.isNotEmpty()
                && lastName.isNotEmpty()
                && email.isNotEmpty()
                && password.isNotEmpty()
                && confirmPassword.isNotEmpty()
                && password == confirmPassword
                && password.length >= 6

        assertTrue("Registration should succeed with valid inputs", isValid)
    }

    // ----- Login Tests -----

    @Test
    fun `login fails with empty email or password`() {
        val email = ""
        val password = ""

        val isValid = email.isNotEmpty() && password.isNotEmpty()
        assertFalse("Login should fail with empty fields", isValid)
    }

    @Test
    fun `login succeeds with valid email and password`() {
        val email = "Kush@unitTest.com"
        val password = "123456"

        val isValid = email.isNotEmpty() && password.isNotEmpty()
        assertTrue("Login should succeed with valid email and password", isValid)
    }

    @Test
    fun `login fails with invalid email format`() {
        val email = "invalid-email"
        val regex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        val isValid = email.matches(regex)

        assertFalse("Login should fail with invalid email format", isValid)
    }
}
