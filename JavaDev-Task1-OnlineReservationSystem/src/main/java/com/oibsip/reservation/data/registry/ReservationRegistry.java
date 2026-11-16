
package com.oibsip.reservation.data.registry;

import com.oibsip.reservation.data.models.Reservation;

import java.sql.SQLException;
import java.util.List;

public interface ReservationRegistry {

    Reservation save(Reservation reservation) throws SQLException;

    Reservation findById(String id) throws SQLException;

    List<Reservation> findByPassengerName(String passengerName) throws SQLException;

    boolean existById(String id) throws SQLException;

    void deleteById(String id) throws SQLException;
}
