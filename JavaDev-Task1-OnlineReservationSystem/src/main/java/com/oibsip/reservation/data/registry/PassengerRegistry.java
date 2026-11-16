package com.oibsip.reservation.data.registry;

import com.oibsip.reservation.data.models.Passenger;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PassengerRegistry {
    Passenger save(Passenger passenger) throws SQLException;

    Passenger findByUsername(String username) throws SQLException;

    Passenger findByEmail(String email) throws SQLException;

    boolean existsByUsername(String username) throws SQLException;

    Passenger findById(String id) throws SQLException;

    List<Passenger> findAll() throws SQLException;

    boolean deleteById(String id) throws SQLException;
}
