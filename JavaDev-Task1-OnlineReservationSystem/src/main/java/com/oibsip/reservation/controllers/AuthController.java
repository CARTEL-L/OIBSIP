package com.oibsip.reservation.controllers;

import com.oibsip.reservation.dtos.request.LoginPassengerRequest;
import com.oibsip.reservation.dtos.request.LogoutPassengerRequest;
import com.oibsip.reservation.dtos.request.RegisterPassengerRequest;
import com.oibsip.reservation.dtos.response.LoginPassengerResponse;
import com.oibsip.reservation.dtos.response.LogoutPassengerResponse;
import com.oibsip.reservation.dtos.response.RegisterPassengerResponse;
import com.oibsip.reservation.services.AuthService;

import java.sql.SQLException;

public class AuthController {

    private final AuthService authService;
    AuthController(AuthService authService) {
        this.authService = authService;
    }

    public RegisterPassengerResponse register(RegisterPassengerRequest registerPassengerRequest) {
        try{
            return authService.register(registerPassengerRequest);
        } catch (SQLException e){
            throw new RuntimeException("Registration failed", e);
        }
    }

    public LoginPassengerResponse login(LoginPassengerRequest loginPassengerRequest) {
        try{
            return authService.login(loginPassengerRequest);
        } catch (SQLException e){
            throw new RuntimeException("Login failed", e);
        }
    }

    public LogoutPassengerResponse logout(LogoutPassengerRequest logoutPassengerRequest) {
        try{
            return authService.logout(logoutPassengerRequest);
        } catch (SQLException e){
            throw new RuntimeException("Logout failed", e);
        }
    }
}
