
package com.oibsip.reservation.data.registry;

import com.oibsip.reservation.data.models.Reservation;
import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.TrainClass;
import com.oibsip.reservation.data.models.TrainName;
import com.oibsip.reservation.database.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationRegistryImpl implements ReservationRegistry {

    @Override
    public Reservation save(Reservation reservation) throws SQLException {

        String sql = "INSERT INTO reservations " +
                "(passengerName, trainNumber, trainName, classType, dateOfJourney, sourceStation, destinationStation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, reservation.getPassengerName());
            statement.setLong(2, reservation.getTrainNumber());
            statement.setString(3, reservation.getTrainName().toString());
            statement.setString(4, reservation.getClassType().toString());
            statement.setDate(5, java.sql.Date.valueOf(reservation.getDateOfJourney()));
            statement.setString(6, reservation.getSourceStation().toString());
            statement.setString(7, reservation.getDestinationStation().toString());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getString(1));
                }
            }
        }

        return reservation;
    }

    @Override
    public Reservation findById(String id) throws SQLException {

        String sql = "SELECT * FROM reservations WHERE id = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapReservation(resultSet);
                }
            }
        }

        return null;
    }

    @Override
    public List<Reservation> findByPassengerName(String passengerName) throws SQLException {

        String sql = "SELECT * FROM reservations WHERE passengerName = ?";

        List<Reservation> reservations = new ArrayList<>();

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passengerName);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
            }
        }

        return reservations;
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {

        Reservation reservation = new Reservation();

        reservation.setId(resultSet.getString("id"));
        reservation.setPassengerName(resultSet.getString("passengerName"));
        reservation.setTrainNumber(resultSet.getLong("trainNumber"));
        reservation.setTrainName(TrainName.valueOf(resultSet.getString("trainName")));
        reservation.setClassType(TrainClass.valueOf(resultSet.getString("classType")));
        reservation.setDateOfJourney(
                resultSet.getDate("dateOfJourney").toLocalDate()
        );
        reservation.setSourceStation(Station.valueOf(resultSet.getString("sourceStation")));
        reservation.setDestinationStation(Station.valueOf(resultSet.getString("destinationStation")));

        return reservation;
    }

    @Override
    public boolean existById(String id) throws SQLException {

        String sql = "SELECT 1 FROM reservations WHERE id = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    @Override
    public void deleteById(String id) throws SQLException {

        String sql = "DELETE FROM reservations WHERE id = ?";

        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.executeUpdate();
        }
    }
}
