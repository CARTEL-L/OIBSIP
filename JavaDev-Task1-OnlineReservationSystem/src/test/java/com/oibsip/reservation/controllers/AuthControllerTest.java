package com.oibsip.reservation.controllers;

import com.oibsip.reservation.exceptions.InvalidLoginRequestError;
import com.oibsip.reservation.exceptions.InvalidRegisterRequestError;
import com.oibsip.reservation.services.AuthServiceImpl;
import com.oibsip.reservation.testsupport.FailingPassengerRegistry;
import com.oibsip.reservation.testsupport.InMemoryPassengerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static com.oibsip.reservation.testsupport.TestData.loginRequest;
import static com.oibsip.reservation.testsupport.TestData.logoutRequest;
import static com.oibsip.reservation.testsupport.TestData.passenger;
import static com.oibsip.reservation.testsupport.TestData.registerRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Same package as AuthController because its constructor is package-private.
class AuthControllerTest {

    private InMemoryPassengerRegistry registry;
    private AuthController controller;
    private AuthController brokenController;

    @BeforeEach
    void setUp() {
        registry = new InMemoryPassengerRegistry();
        controller = new AuthController(new AuthServiceImpl(registry));
        brokenController = new AuthController(new AuthServiceImpl(new FailingPassengerRegistry()));
    }

    @Test
    void registerThenLoginThenLogoutWorksEndToEnd() {
        assertEquals("Registration successful",
                controller.register(registerRequest("lucid", "secret1", "lucid@mail.com")).getMessage());
        assertEquals("Login successful",
                controller.login(loginRequest("lucid", "secret1")).getMessage());
        assertEquals("Logout successful",
                controller.logout(logoutRequest("lucid")).getMessage());
    }

    @Test
    void loginWithBadCredentialsIsDenied() {
        registry.save(passenger("lucid", "secret1", "lucid@mail.com"));

        assertThrows(InvalidLoginRequestError.class,
                () -> controller.login(loginRequest("lucid", "wrong")));
        assertThrows(InvalidLoginRequestError.class,
                () -> controller.login(loginRequest("ghost", "secret1")));
    }

    @Test
    void validationErrorsReachTheCallerUnwrapped() {
        registry.save(passenger("lucid", "secret1", "lucid@mail.com"));

        InvalidRegisterRequestError error = assertThrows(InvalidRegisterRequestError.class,
                () -> controller.register(registerRequest("lucid", "secret1", "new@mail.com")));
        assertEquals("Username already exist", error.getMessage());
    }

    @Test
    void databaseFailureOnRegisterIsWrapped() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> brokenController.register(registerRequest("lucid", "secret1", "lucid@mail.com")));

        assertEquals("Registration failed", error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }

    @Test
    void databaseFailureOnLoginIsWrapped() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> brokenController.login(loginRequest("lucid", "secret1")));

        assertEquals("Login failed", error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }

    @Test
    void databaseFailureOnLogoutIsWrapped() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> brokenController.logout(logoutRequest("lucid")));

        assertEquals("Logout failed", error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }
}
