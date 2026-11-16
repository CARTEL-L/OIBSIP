
package com.oibsip.reservation.services;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.dtos.response.CancelBookingResponse;

import java.sql.SQLException;
import java.util.List;

public interface ReservationService {

    BookTicketResponse bookTickets(BookTicketRequest bookTicketRequest) throws SQLException;

    CancelBookingResponse cancelBooking(CancelBookingRequest cancelRequest) throws SQLException;

    Reservation fetchTicket(CancelBookingRequest cancelRequest) throws SQLException;

    List<Reservation> fetchReservations(String passengerName) throws SQLException;
}
