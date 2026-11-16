
package com.oibsip.reservation.utils;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.models.TrainName;
import com.oibsip.reservation.dtos.request.*;
import com.oibsip.reservation.dtos.response.*;

public class Mappers {

    public static Passenger map(RegisterPassengerRequest registerRequest) {
        Passenger passenger = new Passenger();
        passenger.setUsername(registerRequest.getUsername());
        passenger.setPassword(registerRequest.getPassword());
        passenger.setEmail(registerRequest.getEmail());

        return passenger;
    }

    public static RegisterPassengerResponse mapper(Passenger passenger) {
        RegisterPassengerResponse response = new RegisterPassengerResponse();
        response.setMessage("Registration successful");

        return response;
    }

    public static Passenger map(LoginPassengerRequest loginPassengerRequest) {
        Passenger passenger = new Passenger();
        passenger.setUsername(loginPassengerRequest.getUsername());
        passenger.setPassword(loginPassengerRequest.getPassword());

        return passenger;
    }

    public static LoginPassengerResponse map(Passenger passenger) {
        LoginPassengerResponse loginPassengerResponse = new LoginPassengerResponse();
        passenger.setLoggedIn(true);
        loginPassengerResponse.setMessage("Login successful");

        return loginPassengerResponse;
    }

    public static Passenger map(LogoutPassengerRequest logoutRequest) {
        Passenger passenger = new Passenger();
        passenger.setUsername(logoutRequest.getUsername());

        return passenger;
    }

    public static LogoutPassengerResponse maps(Passenger passenger) {
        LogoutPassengerResponse logoutResponse = new LogoutPassengerResponse();
        passenger.setLoggedIn(false);
        logoutResponse.setMessage("Logout successful");

        return logoutResponse;
    }

    // Reservation mappers

    public static Reservation map(BookTicketRequest bookTicketRequest) {
        Reservation reservation = new Reservation();

        reservation.setPassengerName(bookTicketRequest.getPassengerName());
        reservation.setTrainNumber(bookTicketRequest.getTrainNumber());
        reservation.setTrainName(
                TrainName.findByTrainNumber(bookTicketRequest.getTrainNumber())
        );
        reservation.setClassType(bookTicketRequest.getClassType());
        reservation.setDateOfJourney(bookTicketRequest.getDateOfJourney());
        reservation.setSourceStation(bookTicketRequest.getSourceStation());
        reservation.setDestinationStation(bookTicketRequest.getDestinationStation());

        return reservation;
    }

    public static BookTicketResponse maps(Reservation reservation) {
        BookTicketResponse response = new BookTicketResponse();

        response.setDateOfJourney(reservation.getDateOfJourney());
        response.setTrainNumber(reservation.getTrainNumber());
        response.setDestinationStation(reservation.getDestinationStation());
        response.setMessage("See your PNR below");
        response.setPNR(reservation.getId());

        return response;
    }

    public static CancelBookingResponse maps() {
        CancelBookingResponse response = new CancelBookingResponse();
        response.setMessage("Ticket Cancelled");

        return response;
    }
}
