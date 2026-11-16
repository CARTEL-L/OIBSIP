package com.oibsip.reservation.data.models;

import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.testsupport.TestData;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelsAndDtosTest {

    @Test
    void enumsContainTheValuesTheUiOffers() {
        assertEquals(10, Station.values().length);
        assertEquals(4, TrainClass.values().length);
        assertEquals(3, TrainName.values().length);
        assertEquals(Station.IKEJA, Station.valueOf("IKEJA"));
        assertEquals(TrainClass.FIRST_CLASS, TrainClass.valueOf("FIRST_CLASS"));
    }

    @Test
    void passengerStartsLoggedOutAndStoresItsFields() {
        Passenger passenger = TestData.passenger("lucid", "secret1", "lucid@mail.com");

        assertEquals("lucid", passenger.getUsername());
        assertEquals("secret1", passenger.getPassword());
        assertEquals("lucid@mail.com", passenger.getEmail());
        assertFalse(passenger.isLoggedIn());
        assertNull(passenger.getId());
    }

    @Test
    void reservationsWithTheSameDataAreEqual() {
        Reservation first = TestData.reservation("lucid");
        Reservation second = TestData.reservation("lucid");
        second.setDateOfJourney(first.getDateOfJourney());

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());

        second.setPassengerName("someone else");
        assertNotEquals(first, second);
    }

    @Test
    void reservationStoresAllBookingDetails() {
        Reservation reservation = TestData.reservation("lucid");
        reservation.setId("7");

        assertEquals("7", reservation.getId());
        assertEquals("lucid", reservation.getPassengerName());
        assertEquals(1500L, reservation.getTrainNumber());
        assertEquals(TrainName.CLASSIC, reservation.getTrainName());
        assertEquals(TrainClass.ECONOMY, reservation.getClassType());
        assertEquals(Station.IKEJA, reservation.getSourceStation());
        assertEquals(Station.OSHODI, reservation.getDestinationStation());
        assertTrue(reservation.toString().contains("lucid"));
    }

    @Test
    void requestAndResponseObjectsCarryTheirData() {
        CancelBookingRequest cancel = TestData.cancelRequest("42");
        assertEquals("42", cancel.getPNR());

        LoginPassengerRequest login = TestData.loginRequest("lucid", "secret1");
        assertEquals("lucid", login.getUsername());
        assertEquals("secret1", login.getPassword());

        BookTicketResponse response = new BookTicketResponse();
        response.setPNR("9");
        response.setTrainNumber(2500L);
        response.setDateOfJourney(LocalDate.of(2030, 1, 1));
        response.setDestinationStation(Station.AGEGE);
        response.setMessage("ok");
        assertEquals("9", response.getPNR());
        assertEquals(2500L, response.getTrainNumber());
        assertEquals(LocalDate.of(2030, 1, 1), response.getDateOfJourney());
        assertEquals(Station.AGEGE, response.getDestinationStation());
        assertEquals("ok", response.getMessage());
    }
}
