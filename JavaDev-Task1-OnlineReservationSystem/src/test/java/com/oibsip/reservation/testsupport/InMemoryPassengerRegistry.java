package com.oibsip.reservation.testsupport;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.data.registry.PassengerRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Fake registry backed by a map, so tests need no MySQL server. */
public class InMemoryPassengerRegistry implements PassengerRegistry {

    private final Map<String, Passenger> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Passenger save(Passenger passenger) {
        passenger.setId(String.valueOf(nextId++));
        store.put(passenger.getId(), passenger);
        return passenger;
    }

    @Override
    public Passenger findByUsername(String username) {
        return store.values().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Passenger findByEmail(String email) {
        return store.values().stream()
                .filter(p -> p.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public Passenger findById(String id) {
        return store.get(id);
    }

    @Override
    public List<Passenger> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    public int size() {
        return store.size();
    }
}
