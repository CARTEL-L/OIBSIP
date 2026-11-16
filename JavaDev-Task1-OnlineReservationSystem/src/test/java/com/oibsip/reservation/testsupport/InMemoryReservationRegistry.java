package com.oibsip.reservation.testsupport;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.registry.ReservationRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryReservationRegistry implements ReservationRegistry {

    private final Map<String, Reservation> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Reservation save(Reservation reservation) {
        reservation.setId(String.valueOf(nextId++));
        store.put(reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Reservation findById(String id) {
        return store.get(id);
    }

    @Override
    public List<Reservation> findByPassengerName(String passengerName) {
        List<Reservation> matches = new ArrayList<>();
        for (Reservation reservation : store.values()) {
            if (reservation.getPassengerName().equals(passengerName)) {
                matches.add(reservation);
            }
        }
        return matches;
    }

    @Override
    public boolean existById(String id) {
        return store.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    public int size() {
        return store.size();
    }
}
