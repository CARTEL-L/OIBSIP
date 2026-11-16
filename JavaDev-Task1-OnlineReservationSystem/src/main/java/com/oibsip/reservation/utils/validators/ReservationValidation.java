package com.oibsip.reservation.utils.validators;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.registry.ReservationRegistry;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.exceptions.ReservationError;
import com.oibsip.reservation.utils.Mappers;

import java.sql.SQLException;
import java.time.LocalDate;

public class ReservationValidation {

    private final ReservationRegistry reservationDepot;
    public ReservationValidation(ReservationRegistry reservationDepot) {
        this.reservationDepot = reservationDepot;
    }

    public void validateBookTicket(BookTicketRequest bookTicketRequest){
        if (bookTicketRequest.getDateOfJourney().isBefore(LocalDate.now())){
            throw new ReservationError("Invalid Travelling Date");
        }
    }

    public void validateCancelBooking(CancelBookingRequest cancelRequest)throws SQLException{
        if (!reservationDepot.existById(cancelRequest.getPNR())){
            throw new ReservationError("PNR does not exist");
        }
    }
}
