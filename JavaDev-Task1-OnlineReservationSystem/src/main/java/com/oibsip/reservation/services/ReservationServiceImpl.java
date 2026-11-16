
package com.oibsip.reservation.services;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.registry.ReservationRegistry;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.dtos.response.BookTicketResponse;
import com.oibsip.reservation.dtos.response.CancelBookingResponse;
import com.oibsip.reservation.utils.Mappers;
import com.oibsip.reservation.utils.validators.ReservationValidation;

import java.sql.SQLException;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final ReservationRegistry reservationDepot;

    public ReservationServiceImpl(ReservationRegistry reservationDepot) {
        this.reservationDepot = reservationDepot;
    }

    @Override
    public BookTicketResponse bookTickets(BookTicketRequest bookRequest) throws SQLException {
        ReservationValidation validation = new ReservationValidation(reservationDepot);
        validation.validateBookTicket(bookRequest);

        Reservation reservation = Mappers.map(bookRequest);
        Reservation savedReservation = reservationDepot.save(reservation);

        return Mappers.maps(savedReservation);
    }

    @Override
    public CancelBookingResponse cancelBooking(CancelBookingRequest cancelRequest) throws SQLException {
        ReservationValidation validation = new ReservationValidation(reservationDepot);
        validation.validateCancelBooking(cancelRequest);

        reservationDepot.deleteById(cancelRequest.getPNR());

        return Mappers.maps();
    }

    @Override
    public Reservation fetchTicket(CancelBookingRequest cancelRequest) throws SQLException {
        return reservationDepot.findById(cancelRequest.getPNR());
    }

    @Override
    public List<Reservation> fetchReservations(String passengerName) throws SQLException {
        return reservationDepot.findByPassengerName(passengerName);
    }
}
