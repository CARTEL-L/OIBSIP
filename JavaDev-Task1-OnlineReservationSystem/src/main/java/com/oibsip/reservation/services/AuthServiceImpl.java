package com.oibsip.reservation.services;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.registry.PassengerRegistry;
import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.request.LogoutPassengerRequest;
import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;
import com.oibsip.reservation.dtos.response.LoginPassengerResponse;
import com.oibsip.reservation.dtos.response.LogoutPassengerResponse;
import com.oibsip.reservation.dtos.response.RegisterPassengerResponse;
import com.oibsip.reservation.utils.Mappers;
import com.oibsip.reservation.utils.validators.AuthValidation;

import java.sql.SQLException;

public class AuthServiceImpl implements AuthService{

    private final PassengerRegistry passengerRegistry;
    public AuthServiceImpl(PassengerRegistry passengerRegistry) {
        this.passengerRegistry = passengerRegistry;
    }

    @Override
    public RegisterPassengerResponse register(RegisterPassengerRequest registerRequest) throws SQLException {
        AuthValidation validator = new AuthValidation(passengerRegistry);
        validator.validateRegisterRequest(registerRequest);

        Passenger passenger = Mappers.map(registerRequest);
        Passenger savedPassenger = passengerRegistry.save(passenger);
        return Mappers.mapper(savedPassenger);
    }

    @Override
    public LoginPassengerResponse login(LoginPassengerRequest loginRequest) throws SQLException {
        AuthValidation validator = new AuthValidation(passengerRegistry);
        validator.validateLoginRequest(loginRequest);

        Passenger passenger = Mappers.map(loginRequest);
        Passenger savedPassenger = passengerRegistry.findByUsername(passenger.getUsername());
        return Mappers.map(savedPassenger);
    }

    @Override
    public LogoutPassengerResponse logout(LogoutPassengerRequest logoutRequest) throws SQLException {
        AuthValidation validator = new AuthValidation(passengerRegistry);
        validator.validateLogoutRequest(logoutRequest);

        Passenger passenger = Mappers.map(logoutRequest);
        Passenger savedPassenger = passengerRegistry.findByUsername(passenger.getUsername());
        return Mappers.maps(savedPassenger);
    }
}
