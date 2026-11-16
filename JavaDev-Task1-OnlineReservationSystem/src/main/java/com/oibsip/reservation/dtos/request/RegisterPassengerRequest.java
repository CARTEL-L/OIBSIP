package com.oibsip.reservation.dtos.request;

import lombok.Data;

@Data
public class RegisterPassengerRequest {
    private String username;
    private String password;
    private String email;
}
