package com.oibsip.reservation.testsupport;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.registry.PassengerRegistry;

import java.sql.SQLException;
import java.util.List;

/** Simulates a database outage: every call throws SQLException. */
public class FailingPassengerRegistry implements PassengerRegistry {

    private static SQLException down() {
        return new SQLException("database down");
    }

    @Override
    public Passenger save(Passenger passenger) throws SQLException {
        throw down();
    }

    @Override
    public Passenger findByUsername(String username) throws SQLException {
        throw down();
    }

    @Override
    public Passenger findByEmail(String email) throws SQLException {
        throw down();
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        throw down();
    }

    @Override
    public Passenger findById(String id) throws SQLException {
        throw down();
    }

    @Override
    public List<Passenger> findAll() throws SQLException {
        throw down();
    }

    @Override
    public boolean deleteById(String id) throws SQLException {
        throw down();
    }
}
