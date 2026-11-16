package com.oibsip.reservation.utils;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.TrainClass;
import com.oibsip.reservation.data.models.TrainName;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.dtos.response.CancelBookingResponse;
import com.oibsip.reservation.dtos.response.LoginPassengerResponse;
import com.oibsip.reservation.dtos.response.LogoutPassengerResponse;
import com.oibsip.reservation.dtos.response.RegisterPassengerResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.oibsip.reservation.testsupport.TestData.bookRequest;
import static com.oibsip.reservation.testsupport.TestData.loginRequest;
import static com.oibsip.reservation.testsupport.TestData.logoutRequest;
import static com.oibsip.reservation.testsupport.TestData.passenger;
import static com.oibsip.reservation.testsupport.TestData.registerRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappersTest {

    @Test
    void registerRequestMapsToPassenger() {
        Passenger result = Mappers.map(registerRequest("lucid", "secret1", "lucid@mail.com"));

        assertEquals("lucid", result.getUsername());
        assertEquals("secret1", result.getPassword());
        assertEquals("lucid@mail.com", result.getEmail());
    }

    @Test
    void savedPassengerMapsToRegistrationResponse() {
        RegisterPassengerResponse response = Mappers.mapper(passenger("lucid", "secret1", "lucid@mail.com"));

        assertEquals("Registration successful", response.getMessage());
    }

    @Test
    void loginRequestMapsToPassengerWithCredentials() {
        Passenger result = Mappers.map(loginRequest("lucid", "secret1"));

        assertEquals("lucid", result.getUsername());
        assertEquals("secret1", result.getPassword());
    }

    @Test
    void loginResponseMarksPassengerAsLoggedIn() {
        Passenger passenger = passenger("lucid", "secret1", "lucid@mail.com");

        LoginPassengerResponse response = Mappers.map(passenger);

        assertEquals("Login successful", response.getMessage());
        assertTrue(passenger.isLoggedIn());
    }

    @Test
    void logoutRequestMapsToPassengerWithUsername() {
        Passenger result = Mappers.map(logoutRequest("lucid"));

        assertEquals("lucid", result.getUsername());
    }

    @Test
    void logoutResponseMarksPassengerAsLoggedOut() {
        Passenger passenger = passenger("lucid", "secret1", "lucid@mail.com");
        passenger.setLoggedIn(true);

        LogoutPassengerResponse response = Mappers.maps(passenger);

        assertEquals("Logout successful", response.getMessage());
        assertFalse(passenger.isLoggedIn());
    }

    @Test
    void bookRequestMapsToReservationAndDerivesTrainName() {
        LocalDate date = LocalDate.now().plusDays(5);
        BookTicketRequest request = bookRequest("lucid", 2500L, date);
        request.setClassType(TrainClass.VIP);
        request.setSourceStation(Station.AGEGE);
        request.setDestinationStation(Station.MUSHIN);

        Reservation reservation = Mappers.map(request);

        assertEquals("lucid", reservation.getPassengerName());
        assertEquals(2500L, reservation.getTrainNumber());
        assertEquals(TrainName.PREMIUM, reservation.getTrainName());
        assertEquals(TrainClass.VIP, reservation.getClassType());
        assertEquals(date, reservation.getDateOfJourney());
        assertEquals(Station.AGEGE, reservation.getSourceStation());
        assertEquals(Station.MUSHIN, reservation.getDestinationStation());
        assertNull(reservation.getId());
    }

    @Test
    void bookRequestWithUnknownTrainNumberHasNoTrainName() {
        Reservation reservation = Mappers.map(bookRequest("lucid", 5000L, LocalDate.now().plusDays(1)));

        assertNull(reservation.getTrainName());
    }

    @Test
    void savedReservationMapsToResponseWithPnr() {
        LocalDate date = LocalDate.now().plusDays(2);
        Reservation reservation = Mappers.map(bookRequest("lucid", 1500L, date));
        reservation.setId("123");

        BookTicketResponse response = Mappers.maps(reservation);

        assertEquals("123", response.getPNR());
        assertEquals(1500L, response.getTrainNumber());
        assertEquals(date, response.getDateOfJourney());
        assertEquals(Station.OSHODI, response.getDestinationStation());
        assertEquals("See your PNR below", response.getMessage());
    }

    @Test
    void cancellationResponseHasConfirmationMessage() {
        CancelBookingResponse response = Mappers.maps();

        assertEquals("Ticket Cancelled", response.getMessage());
    }
}
