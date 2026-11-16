package com.oibsip.reservation.data.registry;

import com.oibsip.reservation.data.models.Passenger;
import com.oibsip.reservation.database.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PassengerRegistryImpl implements PassengerRegistry {

    @Override
    public Passenger save(Passenger passenger) throws SQLException {
        String sql = "INSERT INTO passengers (username, password, email) VALUES (?, ?, ?)";

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, passenger.getUsername());
        statement.setString(2, passenger.getPassword());
        statement.setString(3, passenger.getEmail());

        statement.executeUpdate();
        }

        return passenger;
    }

    @Override
    public Passenger findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM passengers WHERE username = ?";

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Passenger passenger = new Passenger();
                    passenger.setId(resultSet.getString("id"));
                    passenger.setUsername(resultSet.getString("username"));
                    passenger.setPassword(resultSet.getString("password"));
                    passenger.setEmail(resultSet.getString("email"));

                    return passenger;
                }
            }
        }
        return null;
    }

    @Override
    public Passenger findByEmail(String email) throws SQLException{
        String sql = "SELECT * FROM passengers WHERE email = ?";

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Passenger passenger = new Passenger();
                    passenger.setId(resultSet.getString("id"));
                    passenger.setUsername(resultSet.getString("username"));
                    passenger.setPassword(resultSet.getString("password"));
                    passenger.setEmail(resultSet.getString("email"));

                    return passenger;
                }
            }
        }
        return null;
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT 1 FROM passengers WHERE username = ?";

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public Passenger findById(String id) throws SQLException {
        String sql = "SELECT * FROM passengers WHERE id = ?";
        try(Connection connection = DataBaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Passenger passenger = new Passenger();
                    passenger.setId(resultSet.getString("id"));
                    passenger.setUsername(resultSet.getString("username"));
                    passenger.setPassword(resultSet.getString("password"));
                    passenger.setEmail(resultSet.getString("email"));

                    return passenger;
                }
            }
        }
        return null;
    }

    @Override
    public List<Passenger> findAll() throws SQLException {
        String sql = "SELECT * FROM passengers";
        List<Passenger> passengers = new ArrayList<>();

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Passenger passenger = new Passenger();
                passenger.setId(resultSet.getString("id"));
                passenger.setUsername(resultSet.getString("username"));
                passenger.setPassword(resultSet.getString("password"));
                passenger.setEmail(resultSet.getString("email"));

                passengers.add(passenger);
            }
        }

        return passengers;
    }

    @Override
    public boolean deleteById(String id) throws SQLException {
        String sql = "DELETE FROM passengers WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}
