package com.oibsip.reservation.data.registry;

import com.oibsip.reservation.data.models.Train;

import java.sql.SQLException;
import java.util.List;

public interface TrainRegistry {
    Train save(Train train)throws SQLException;

    Train findByTrainName(String trainName)throws SQLException;

    boolean existsByTrainNumber(long trainNumber)throws SQLException;

    Train findById(String id)throws SQLException;

    List<Train> findAll()throws SQLException;

    void deleteById(String id)throws SQLException;
}
