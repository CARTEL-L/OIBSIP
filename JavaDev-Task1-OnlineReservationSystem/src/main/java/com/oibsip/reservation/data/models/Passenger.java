package com.oibsip.reservation.data.models;

import lombok.Data;

@Data
public class Passenger {

    private String id;
    private String username;
    private String password;
    private String email;
    private boolean loggedIn;
}
