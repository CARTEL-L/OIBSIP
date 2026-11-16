package com.oibsip.reservation.exceptions;

public class InvalidRegisterRequestError extends RuntimeException {
    public InvalidRegisterRequestError(String message) {
        super(message);
    }
}
