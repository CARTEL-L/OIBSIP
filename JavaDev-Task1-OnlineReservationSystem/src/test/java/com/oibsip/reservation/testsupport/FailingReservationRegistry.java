package com.oibsip.reservation.testsupport;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.registry.ReservationRegistry;

import java.sql.SQLException;
import java.util.List;

/** Simulates a database outage: every call throws SQLException. */
public class FailingReservationRegistry implements ReservationRegistry {

    private static SQLException down() {
        return new SQLException("database down");
    }

    @Override
    public Reservation save(Reservation reservation) throws SQLException {
        throw down();
    }

    @Override
    public Reservation findById(String id) throws SQLException {
        throw down();
    }

    @Override
    public List<Reservation> findByPassengerName(String passengerName) throws SQLException {
        throw down();
    }

    @Override
    public boolean existById(String id) throws SQLException {
        throw down();
    }

    @Override
    public void deleteById(String id) throws SQLException {
        throw down();
    }
}
