package com.oibsip.reservation.exceptions;

public class InvalidLoginRequestError extends RuntimeException {
    public InvalidLoginRequestError(String message) {
        super(message);
    }
}
