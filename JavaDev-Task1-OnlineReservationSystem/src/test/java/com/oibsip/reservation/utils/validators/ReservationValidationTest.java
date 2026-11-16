package com.oibsip.reservation.utils.validators;

import com.oibsip.reservation.exceptions.ReservationError;
import com.oibsip.reservation.testsupport.InMemoryReservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.oibsip.reservation.testsupport.TestData.bookRequest;
import static com.oibsip.reservation.testsupport.TestData.cancelRequest;
import static com.oibsip.reservation.testsupport.TestData.reservation;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationValidationTest {

    private InMemoryReservationRegistry registry;
    private ReservationValidation validation;

    @BeforeEach
    void setUp() {
        registry = new InMemoryReservationRegistry();
        validation = new ReservationValidation(registry);
    }

    @Test
    void journeyDateInThePastIsRejected() {
        ReservationError error = assertThrows(ReservationError.class, () ->
                validation.validateBookTicket(bookRequest("lucid", 1500L, LocalDate.now().minusDays(1))));

        assertEquals("Invalid Travelling Date", error.getMessage());
    }

    @Test
    void journeyDateOfTodayIsAccepted() {
        assertDoesNotThrow(() ->
                validation.validateBookTicket(bookRequest("lucid", 1500L, LocalDate.now())));
    }

    @Test
    void journeyDateInTheFutureIsAccepted() {
        assertDoesNotThrow(() ->
                validation.validateBookTicket(bookRequest("lucid", 1500L, LocalDate.now().plusMonths(2))));
    }

    @Test
    void cancellingAnUnknownPnrIsRejected() {
        ReservationError error = assertThrows(ReservationError.class, () ->
                validation.validateCancelBooking(cancelRequest("999")));

        assertEquals("PNR does not exist", error.getMessage());
    }

    @Test
    void cancellingAnExistingPnrIsAccepted() {
        String pnr = registry.save(reservation("lucid")).getId();

        assertDoesNotThrow(() -> validation.validateCancelBooking(cancelRequest(pnr)));
    }
}
