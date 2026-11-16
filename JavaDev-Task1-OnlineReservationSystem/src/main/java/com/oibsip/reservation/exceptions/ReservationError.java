package com.oibsip.reservation.exceptions;

public class ReservationError extends RuntimeException {
    public ReservationError(String message) {
        super(message);
    }
}
