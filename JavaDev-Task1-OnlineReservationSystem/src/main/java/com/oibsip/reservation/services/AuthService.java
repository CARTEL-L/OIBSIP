package com.oibsip.reservation.services;

import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.request.LogoutPassengerRequest;
import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;
import com.oibsip.reservation.dtos.response.LoginPassengerResponse;
import com.oibsip.reservation.dtos.response.LogoutPassengerResponse;
import com.oibsip.reservation.dtos.response.RegisterPassengerResponse;

import java.sql.SQLException;

public interface AuthService {
    RegisterPassengerResponse register(RegisterPassengerRequest registerRequest) throws SQLException;

    LoginPassengerResponse login(LoginPassengerRequest loginRequest) throws SQLException;

    LogoutPassengerResponse logout(LogoutPassengerRequest logoutRequest) throws SQLException;
}
