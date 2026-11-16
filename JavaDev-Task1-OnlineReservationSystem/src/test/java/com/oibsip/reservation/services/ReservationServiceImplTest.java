package com.oibsip.reservation.services;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.TrainName;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.dtos.response.CancelBookingResponse;
import com.oibsip.reservation.exceptions.ReservationError;
import com.oibsip.reservation.testsupport.InMemoryReservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.oibsip.reservation.testsupport.TestData.bookRequest;
import static com.oibsip.reservation.testsupport.TestData.cancelRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceImplTest {

    private InMemoryReservationRegistry registry;
    private ReservationServiceImpl service;
    private LocalDate futureDate;

    @BeforeEach
    void setUp() {
        registry = new InMemoryReservationRegistry();
        service = new ReservationServiceImpl(registry);
        futureDate = LocalDate.now().plusDays(7);
    }

    // ---------- booking ----------

    @Test
    void bookingSavesTheReservationAndReturnsAPnr() throws Exception {
        BookTicketResponse response = service.bookTickets(bookRequest("lucid", 1500L, futureDate));

        assertNotNull(response.getPNR());
        assertEquals("See your PNR below", response.getMessage());
        assertEquals(1500L, response.getTrainNumber());
        assertEquals(futureDate, response.getDateOfJourney());
        assertEquals(Station.OSHODI, response.getDestinationStation());
        assertEquals(1, registry.size());
    }

    @Test
    void bookedReservationStoresTheDerivedTrainName() throws Exception {
        BookTicketResponse response = service.bookTickets(bookRequest("lucid", 3200L, futureDate));

        Reservation saved = registry.findById(response.getPNR());
        assertNotNull(saved);
        assertEquals("lucid", saved.getPassengerName());
        assertEquals(TrainName.LOGISTICS, saved.getTrainName());
    }

    @Test
    void everyBookingGetsAUniquePnr() throws Exception {
        String first = service.bookTickets(bookRequest("lucid", 1500L, futureDate)).getPNR();
        String second = service.bookTickets(bookRequest("lucid", 1500L, futureDate)).getPNR();

        assertNotEquals(first, second);
        assertEquals(2, registry.size());
    }

    @Test
    void bookingForAPastDateIsRejectedAndNothingIsSaved() {
        ReservationError error = assertThrows(ReservationError.class, () ->
                service.bookTickets(bookRequest("lucid", 1500L, LocalDate.now().minusDays(1))));

        assertEquals("Invalid Travelling Date", error.getMessage());
        assertEquals(0, registry.size());
    }

    @Test
    void bookingForTodayIsAllowed() throws Exception {
        BookTicketResponse response = service.bookTickets(bookRequest("lucid", 1500L, LocalDate.now()));

        assertNotNull(response.getPNR());
    }

    // ---------- fetching ----------

    @Test
    void fetchTicketReturnsTheBooking() throws Exception {
        String pnr = service.bookTickets(bookRequest("lucid", 2100L, futureDate)).getPNR();

        Reservation found = service.fetchTicket(cancelRequest(pnr));

        assertNotNull(found);
        assertEquals(pnr, found.getId());
        assertEquals("lucid", found.getPassengerName());
        assertEquals(TrainName.PREMIUM, found.getTrainName());
    }

    @Test
    void fetchTicketReturnsNullForUnknownPnr() throws Exception {
        assertNull(service.fetchTicket(cancelRequest("999")));
    }

    @Test
    void fetchReservationsReturnsOnlyThatPassengersBookings() throws Exception {
        service.bookTickets(bookRequest("lucid", 1500L, futureDate));
        service.bookTickets(bookRequest("lucid", 2500L, futureDate));
        service.bookTickets(bookRequest("someone", 3500L, futureDate));

        List<Reservation> lucids = service.fetchReservations("lucid");

        assertEquals(2, lucids.size());
        assertTrue(lucids.stream().allMatch(r -> r.getPassengerName().equals("lucid")));
    }

    @Test
    void fetchReservationsIsEmptyForPassengerWithNoBookings() throws Exception {
        assertTrue(service.fetchReservations("nobody").isEmpty());
    }

    // ---------- cancelling ----------

    @Test
    void cancellingRemovesTheBooking() throws Exception {
        String pnr = service.bookTickets(bookRequest("lucid", 1500L, futureDate)).getPNR();

        CancelBookingResponse response = service.cancelBooking(cancelRequest(pnr));

        assertEquals("Ticket Cancelled", response.getMessage());
        assertNull(service.fetchTicket(cancelRequest(pnr)));
        assertEquals(0, registry.size());
    }

    @Test
    void cancellingOnlyRemovesTheChosenBooking() throws Exception {
        String keep = service.bookTickets(bookRequest("lucid", 1500L, futureDate)).getPNR();
        String remove = service.bookTickets(bookRequest("lucid", 2500L, futureDate)).getPNR();

        service.cancelBooking(cancelRequest(remove));

        assertNotNull(service.fetchTicket(cancelRequest(keep)));
        assertNull(service.fetchTicket(cancelRequest(remove)));
    }

    @Test
    void cancellingAnUnknownPnrIsRejected() {
        ReservationError error = assertThrows(ReservationError.class, () ->
                service.cancelBooking(cancelRequest("999")));

        assertEquals("PNR does not exist", error.getMessage());
    }

    @Test
    void cancellingTheSameBookingTwiceFailsTheSecondTime() throws Exception {
        String pnr = service.bookTickets(bookRequest("lucid", 1500L, futureDate)).getPNR();
        service.cancelBooking(cancelRequest(pnr));

        assertThrows(ReservationError.class, () -> service.cancelBooking(cancelRequest(pnr)));
    }
}
