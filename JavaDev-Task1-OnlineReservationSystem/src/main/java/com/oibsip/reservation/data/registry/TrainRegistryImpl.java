package com.oibsip.reservation.data.registry;

import com.oibsip.reservation.data.models.Train;
import com.oibsip.reservation.database.DataBaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TrainRegistryImpl implements TrainRegistry {

    @Override
    public Train save(Train train)throws SQLException {
        String sql = "INSERT INTO trains (trainName, trainNumber) VALUES (?, ?)";

        try(Connection connection = DataBaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, train.getTrainName());
            statement.setLong(2, train.getTrainNumber());

            statement.executeUpdate();
        }

        return  train;
    }


    @Override
    public Train findByTrainName(String trainName)throws SQLException{
        String sql = "SELECT * FROM trains WHERE trainName = ?";

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trainName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    Train train = new Train();
                    train.setId(resultSet.getString("id"));
                    train.setTrainNumber(resultSet.getLong("trainNumber"));
                    train.setTrainName(resultSet.getString("trainName"));

                    return train;
                }
            }
        }

        return null;
    }

    @Override
    public boolean existsByTrainNumber(long trainNumber)throws SQLException{
        String sql = "SELECT * FROM trains WHERE trainNumber = ?";
        try(Connection connection = DataBaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, trainNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return (resultSet.next());
            }
        }
    }

    @Override
    public Train findById(String id)throws SQLException{
        String sql = "SELECT * FROM trains WHERE id = ?";

        try(Connection connection = DataBaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    Train train = new Train();
                    train.setId(resultSet.getString("id"));
                    train.setTrainNumber(resultSet.getLong("trainNumber"));
                    train.setTrainName(resultSet.getString("trainName"));

                    return train;
                }
            }
        }

        return null;
    }

    @Override
    public List<Train> findAll()throws SQLException{
        String sql = "SELECT * FROM trains";
        List<Train> trains = new ArrayList<>();

        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery()) {
            while(resultSet.next()) {
                Train train = new Train();
                train.setId(resultSet.getString("id"));
                train.setTrainNumber(resultSet.getLong("trainNumber"));
                train.setTrainName(resultSet.getString("trainName"));

                trains.add(train);
            }
        }

        return trains;
    }

    @Override
    public void deleteById(String id)throws SQLException{
        String sql = "DELETE FROM trains WHERE id = ?";
        try(Connection connection = DataBaseConnection.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
        }
    }
}
