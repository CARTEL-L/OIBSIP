package com.oibsip.reservation.exceptions;

public class InvalidLogoutRequestError extends RuntimeException {
    public InvalidLogoutRequestError(String message) {
        super(message);
    }
}
