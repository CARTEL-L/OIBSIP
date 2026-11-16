package com.oibsip.reservation.data.models;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Reservation {

    private String id;
    private String passengerName;
    private Long trainNumber;
    private TrainName trainName;
    private TrainClass classType;
    private LocalDate dateOfJourney;
    private Station sourceStation;
    private Station destinationStation;
}
