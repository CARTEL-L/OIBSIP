package com.oibsip.reservation.testsupport;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.TrainClass;
import com.oibsip.reservation.data.models.TrainName;
import com.oibsip.reservation.dtos.request.BookTicketRequest;
import com.oibsip.reservation.dtos.request.CancelBookingRequest;
import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.request.LogoutPassengerRequest;
import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;

import java.time.LocalDate;

public final class TestData {

    private TestData() {
    }

    public static Passenger passenger(String username, String password, String email) {
        Passenger passenger = new Passenger();
        passenger.setUsername(username);
        passenger.setPassword(password);
        passenger.setEmail(email);
        return passenger;
    }

    public static RegisterPassengerRequest registerRequest(String username, String password, String email) {
        RegisterPassengerRequest request = new RegisterPassengerRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setEmail(email);
        return request;
    }

    public static LoginPassengerRequest loginRequest(String username, String password) {
        LoginPassengerRequest request = new LoginPassengerRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    public static LogoutPassengerRequest logoutRequest(String username) {
        LogoutPassengerRequest request = new LogoutPassengerRequest();
        request.setUsername(username);
        return request;
    }

    public static CancelBookingRequest cancelRequest(String pnr) {
        CancelBookingRequest request = new CancelBookingRequest();
        request.setPNR(pnr);
        return request;
    }

    public static BookTicketRequest bookRequest(String passengerName, long trainNumber, LocalDate date) {
        BookTicketRequest request = new BookTicketRequest();
        request.setPassengerName(passengerName);
        request.setTrainNumber(trainNumber);
        request.setClassType(TrainClass.ECONOMY);
        request.setDateOfJourney(date);
        request.setSourceStation(Station.IKEJA);
        request.setDestinationStation(Station.OSHODI);
        return request;
    }

    public static Reservation reservation(String passengerName) {
        Reservation reservation = new Reservation();
        reservation.setPassengerName(passengerName);
        reservation.setTrainNumber(1500L);
        reservation.setTrainName(TrainName.CLASSIC);
        reservation.setClassType(TrainClass.ECONOMY);
        reservation.setDateOfJourney(LocalDate.now().plusDays(3));
        reservation.setSourceStation(Station.IKEJA);
        reservation.setDestinationStation(Station.OSHODI);
        return reservation;
    }
}
