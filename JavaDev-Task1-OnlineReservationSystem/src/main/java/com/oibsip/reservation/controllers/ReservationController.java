package com.oibsip.reservation.controllers;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.dtos.response.CancelBookingResponse;
import com.oibsip.reservation.services.ReservationService;

import java.sql.SQLException;

public class ReservationController {

    private final ReservationService reservationService;
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public BookTicketResponse bookTicket(BookTicketRequest bookTicketRequest) {
        try {
            return reservationService.bookTickets(bookTicketRequest);
        } catch (SQLException e){
            throw new RuntimeException("Unable to book ticket", e);
        }
    }

    public CancelBookingResponse cancelBooking(CancelBookingRequest cancelBookingRequest) {
        try {
            return reservationService.cancelBooking(cancelBookingRequest);
        } catch (SQLException e){
            throw new RuntimeException("Cancellation failed", e);
        }
    }

    public Reservation viewTicket(CancelBookingRequest cancelBookingRequest) {
        try {
            return reservationService.fetchTicket(cancelBookingRequest);
        } catch (SQLException e){
            throw new RuntimeException("Unable to fetch data", e);
        }
    }
}
