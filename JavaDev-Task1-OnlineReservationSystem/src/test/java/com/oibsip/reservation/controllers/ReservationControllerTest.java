package com.oibsip.reservation.controllers;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.exceptions.ReservationError;
import com.oibsip.reservation.services.ReservationServiceImpl;
import com.oibsip.reservation.testsupport.FailingReservationRegistry;
import com.oibsip.reservation.testsupport.InMemoryReservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static com.oibsip.reservation.testsupport.TestData.bookRequest;
import static com.oibsip.reservation.testsupport.TestData.cancelRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservationControllerTest {

    private InMemoryReservationRegistry registry;
    private ReservationController controller;
    private ReservationController brokenController;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        registry = new InMemoryReservationRegistry();
        controller = new ReservationController(new ReservationServiceImpl(registry));
        brokenController = new ReservationController(new ReservationServiceImpl(new FailingReservationRegistry()));
        futureDate = LocalDate.now().plusDays(10);
    }

    @Test
    void bookViewAndCancelWorkEndToEnd() {
        BookTicketResponse booked = controller.bookTicket(bookRequest("lucid", 1500L, futureDate));
        String pnr = booked.getPNR();
        assertNotNull(pnr);

        Reservation viewed = controller.viewTicket(cancelRequest(pnr));
        assertNotNull(viewed);
        assertEquals("lucid", viewed.getPassengerName());

        assertEquals("Ticket Cancelled", controller.cancelBooking(cancelRequest(pnr)).getMessage());
        assertNull(controller.viewTicket(cancelRequest(pnr)));
    }

    @Test
    void viewingAnUnknownPnrReturnsNull() {
        assertNull(controller.viewTicket(cancelRequest("999")));
    }

    @Test
    void bookingInThePastIsRejected() {
        ReservationError error = assertThrows(ReservationError.class, () ->
                controller.bookTicket(bookRequest("lucid", 1500L, LocalDate.now().minusDays(1))));

        assertEquals("Invalid Travelling Date", error.getMessage());
    }

    @Test
    void cancellingAnUnknownPnrIsRejected() {
        ReservationError error = assertThrows(ReservationError.class, () ->
                controller.cancelBooking(cancelRequest("999")));

        assertEquals("PNR does not exist", error.getMessage());
    }

    @Test
    void databaseFailureWhileBookingIsWrapped() {
        RuntimeException error = assertThrows(RuntimeException.class, () ->
                brokenController.bookTicket(bookRequest("lucid", 1500L, futureDate)));

        assertEquals("Unable to book ticket", error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }

    @Test
    void databaseFailureWhileCancellingIsWrapped() {
        RuntimeException error = assertThrows(RuntimeException.class, () ->
                brokenController.cancelBooking(cancelRequest("1")));

        assertEquals("Cancellation failed", error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }

    @Test
    void databaseFailureWhileFetchingIsWrapped() {
        RuntimeException error = assertThrows(RuntimeException.class, () ->
                brokenController.viewTicket(cancelRequest("1")));

        assertEquals("Unable to fetch data", error.getMessage());
        assertInstanceOf(SQLException.class, error.getCause());
    }
}
