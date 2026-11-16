package com.oibsip.reservation.services;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.dtos.response.LoginPassengerResponse;
import com.oibsip.reservation.dtos.response.LogoutPassengerResponse;
import com.oibsip.reservation.dtos.response.RegisterPassengerResponse;
import com.oibsip.reservation.exceptions.InvalidLoginRequestError;
import com.oibsip.reservation.exceptions.InvalidRegisterRequestError;
import com.oibsip.reservation.testsupport.InMemoryPassengerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.oibsip.reservation.testsupport.TestData.loginRequest;
import static com.oibsip.reservation.testsupport.TestData.logoutRequest;
import static com.oibsip.reservation.testsupport.TestData.passenger;
import static com.oibsip.reservation.testsupport.TestData.registerRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceImplTest {

    private InMemoryPassengerRegistry registry;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        registry = new InMemoryPassengerRegistry();
        service = new AuthServiceImpl(registry);
    }

    @Test
    void registerSavesThePassengerAndConfirms() throws Exception {
        RegisterPassengerResponse response =
                service.register(registerRequest("lucid", "secret1", "lucid@mail.com"));

        assertEquals("Registration successful", response.getMessage());
        assertEquals(1, registry.size());

        Passenger saved = registry.findByUsername("lucid");
        assertNotNull(saved);
        assertEquals("lucid@mail.com", saved.getEmail());
        assertNotNull(saved.getId());
    }

    @Test
    void registerRejectsDuplicateUsernameAndSavesNothingExtra() throws Exception {
        service.register(registerRequest("lucid", "secret1", "lucid@mail.com"));

        assertThrows(InvalidRegisterRequestError.class, () ->
                service.register(registerRequest("lucid", "another1", "other@mail.com")));

        assertEquals(1, registry.size());
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        service.register(registerRequest("lucid", "secret1", "lucid@mail.com"));

        assertThrows(InvalidRegisterRequestError.class, () ->
                service.register(registerRequest("other", "another1", "lucid@mail.com")));
    }

    @Test
    void registerRejectsInvalidInputAndSavesNothing() {
        assertThrows(InvalidRegisterRequestError.class, () ->
                service.register(registerRequest("ab", "secret1", "lucid@mail.com")));

        assertEquals(0, registry.size());
    }

    @Test
    void loginSucceedsWithCorrectCredentials() throws Exception {
        registry.save(passenger("lucid", "secret1", "lucid@mail.com"));

        LoginPassengerResponse response = service.login(loginRequest("lucid", "secret1"));

        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void loginIsDeniedForWrongPassword() {
        registry.save(passenger("lucid", "secret1", "lucid@mail.com"));

        assertThrows(InvalidLoginRequestError.class, () ->
                service.login(loginRequest("lucid", "wrong")));
    }

    @Test
    void loginIsDeniedForUnknownUser() {
        assertThrows(InvalidLoginRequestError.class, () ->
                service.login(loginRequest("ghost", "secret1")));
    }

    @Test
    void registeredUserCanLogInImmediately() throws Exception {
        service.register(registerRequest("lucid", "secret1", "lucid@mail.com"));

        LoginPassengerResponse response = service.login(loginRequest("lucid", "secret1"));

        assertEquals("Login successful", response.getMessage());
    }

    @Test
    void logoutSucceedsForExistingUser() throws Exception {
        registry.save(passenger("lucid", "secret1", "lucid@mail.com"));

        LogoutPassengerResponse response = service.logout(logoutRequest("lucid"));

        assertEquals("Logout successful", response.getMessage());
    }

    @Test
    void logoutFailsForUnknownUser() {
        assertThrows(InvalidLoginRequestError.class, () ->
                service.logout(logoutRequest("ghost")));
    }
}
