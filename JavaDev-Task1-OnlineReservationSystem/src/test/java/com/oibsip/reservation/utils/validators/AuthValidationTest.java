package com.oibsip.reservation.utils.validators;

import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;
import com.oibsip.reservation.exceptions.InvalidLoginRequestError;
import com.oibsip.reservation.exceptions.InvalidRegisterRequestError;
import com.oibsip.reservation.testsupport.InMemoryPassengerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.oibsip.reservation.testsupport.TestData.loginRequest;
import static com.oibsip.reservation.testsupport.TestData.logoutRequest;
import static com.oibsip.reservation.testsupport.TestData.passenger;
import static com.oibsip.reservation.testsupport.TestData.registerRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthValidationTest {

    private InMemoryPassengerRegistry registry;
    private AuthValidation validation;

    @BeforeEach
    void setUp() {
        registry = new InMemoryPassengerRegistry();
        validation = new AuthValidation(registry);
        registry.save(passenger("lucid", "secret1", "lucid@mail.com"));
    }

    private void assertRegisterRejected(String expectedMessage, RegisterPassengerRequest request) {
        InvalidRegisterRequestError error = assertThrows(
                InvalidRegisterRequestError.class,
                () -> validation.validateRegisterRequest(request));
        assertEquals(expectedMessage, error.getMessage());
    }

    @Nested
    class Registration {

        @Test
        void validRequestIsAccepted() {
            assertDoesNotThrow(() ->
                    validation.validateRegisterRequest(registerRequest("newuser", "abc123", "new@mail.com")));
        }

        @Test
        void duplicateEmailIsRejected() {
            assertRegisterRejected("Email already in use",
                    registerRequest("newuser", "abc123", "lucid@mail.com"));
        }

        @Test
        void duplicateUsernameIsRejected() {
            assertRegisterRejected("Username already exist",
                    registerRequest("lucid", "abc123", "other@mail.com"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "   "})
        void blankUsernameIsRejected(String username) {
            assertRegisterRejected("Invalid username",
                    registerRequest(username, "abc123", "new@mail.com"));
        }

        @Test
        void usernameShorterThanThreeCharactersIsRejected() {
            assertRegisterRejected("Username cannot be less than 3 characters",
                    registerRequest("ab", "abc123", "new@mail.com"));
        }

        @Test
        void usernameOfExactlyThreeCharactersIsAccepted() {
            assertDoesNotThrow(() ->
                    validation.validateRegisterRequest(registerRequest("abc", "abc123", "new@mail.com")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "      "})
        void blankPasswordIsRejected(String password) {
            assertRegisterRejected("Invalid password",
                    registerRequest("newuser", password, "new@mail.com"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "12345", "12345678901234567"})
        void passwordOutsideSixToSixteenCharactersIsRejected(String password) {
            assertRegisterRejected("Password must be between 6 and 16 characters",
                    registerRequest("newuser", password, "new@mail.com"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"123456", "1234567890123456"})
        void passwordBoundaryLengthsAreAccepted(String password) {
            assertDoesNotThrow(() ->
                    validation.validateRegisterRequest(registerRequest("newuser", password, "new@mail.com")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "mail.com", "no-at-sign"})
        void invalidEmailIsRejected(String email) {
            assertRegisterRejected("Invalid email",
                    registerRequest("newuser", "abc123", email));
        }
    }

    @Nested
    class Login {

        @Test
        void correctCredentialsAreAccepted() {
            assertDoesNotThrow(() -> validation.validateLoginRequest(loginRequest("lucid", "secret1")));
        }

        @Test
        void unknownUsernameIsDenied() {
            InvalidLoginRequestError error = assertThrows(InvalidLoginRequestError.class,
                    () -> validation.validateLoginRequest(loginRequest("ghost", "secret1")));
            assertEquals("Access Denied", error.getMessage());
        }

        @Test
        void wrongPasswordIsDenied() {
            InvalidLoginRequestError error = assertThrows(InvalidLoginRequestError.class,
                    () -> validation.validateLoginRequest(loginRequest("lucid", "wrong")));
            assertEquals("Access Denied. Invalid password", error.getMessage());
        }

        @Test
        void passwordIsCaseSensitive() {
            assertThrows(InvalidLoginRequestError.class,
                    () -> validation.validateLoginRequest(loginRequest("lucid", "SECRET1")));
        }
    }

    @Nested
    class Logout {

        @Test
        void existingAccountCanLogOut() {
            assertDoesNotThrow(() -> validation.validateLogoutRequest(logoutRequest("lucid")));
        }

        @Test
        void unknownAccountIsRejected() {
            InvalidLoginRequestError error = assertThrows(InvalidLoginRequestError.class,
                    () -> validation.validateLogoutRequest(logoutRequest("ghost")));
            assertEquals("Account does not exist", error.getMessage());
        }
    }
}
