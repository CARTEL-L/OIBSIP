package com.oibsip.reservation.dtos.request;

import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.Train;
import com.oibsip.reservation.data.models.TrainClass;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookTicketRequest {
    private String passengerName;
    private long trainNumber;
    private Train  trainName;
    private TrainClass classType;
    private LocalDate dateOfJourney;
    private Station sourceStation;
    private Station destinationStation;
}
