package com.oibsip.reservation.utils.validators;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.registry.PassengerRegistry;
import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.request.LogoutPassengerRequest;
import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;
import com.oibsip.reservation.exceptions.InvalidLoginRequestError;
import com.oibsip.reservation.exceptions.InvalidRegisterRequestError;

import java.sql.SQLException;

public class AuthValidation {

    private final PassengerRegistry passengerRegistry;
    public AuthValidation(PassengerRegistry passengerRegistry) {
        this.passengerRegistry = passengerRegistry;
    }


    public void validateRegisterRequest(RegisterPassengerRequest registerRequest) throws SQLException {
        Passenger passenger = passengerRegistry.findByEmail(registerRequest.getEmail());
        if (passenger != null){
            throw new InvalidRegisterRequestError("Email already in use");
        }
        if (passengerRegistry.findByUsername(registerRequest.getUsername()) != null){
            throw new InvalidRegisterRequestError("Username already exist");
        }
        if (registerRequest.getUsername().isBlank()){
            throw new InvalidRegisterRequestError("Invalid username");
        }
        if (registerRequest.getUsername().length() < 3){
            throw new InvalidRegisterRequestError("Username cannot be less than 3 characters");
        }
        if (registerRequest.getPassword().isBlank()){
            throw new InvalidRegisterRequestError("Invalid password");
        }
        if (registerRequest.getPassword().length() < 6 || registerRequest.getPassword().length() > 16){
            throw new InvalidRegisterRequestError("Password must be between 6 and 16 characters");
        }
        if (registerRequest.getEmail().isBlank()){
            throw new InvalidRegisterRequestError("Invalid email");
        }
        if (!registerRequest.getEmail().contains("@")){
            throw new InvalidRegisterRequestError("Invalid email");
        }
    }

    public void validateLoginRequest(LoginPassengerRequest loginRequest) throws SQLException {
        Passenger passenger = passengerRegistry.findByUsername(loginRequest.getUsername());
        if (passenger == null) {
            throw new InvalidLoginRequestError("Access Denied");
        }
        if (!passenger.getPassword().equals(loginRequest.getPassword())) {
            throw new InvalidLoginRequestError("Access Denied. Invalid password");
        }
        if (!passenger.getUsername().equals(loginRequest.getUsername())) {
            throw new InvalidLoginRequestError("Access Denied. Invalid username");
        }
    }

    public void validateLogoutRequest(LogoutPassengerRequest logoutRequest) throws SQLException {
        Passenger passenger = passengerRegistry.findByUsername(logoutRequest.getUsername());
        if (passenger == null) {
            throw new InvalidLoginRequestError("Account does not exist");
        }
        if (!passenger.getUsername().equals(logoutRequest.getUsername())){
            throw new InvalidLoginRequestError("Invalid username");
        }
    }
}
