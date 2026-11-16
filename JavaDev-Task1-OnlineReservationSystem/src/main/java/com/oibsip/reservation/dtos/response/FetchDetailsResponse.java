package com.oibsip.reservation.dtos.response;

import com.oibsip.reservation.data.models.Station;
import com.oibsip.reservation.data.models.TrainClass;
import com.oibsip.reservation.data.models.TrainName;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FetchDetailsResponse {

    private String pnr;
    private String passengerName;
    private Long trainNumber;
    private TrainName trainName;
    private TrainClass classType;
    private LocalDate dateOfJourney;
    private Station sourceStation;
    private Station destinationStation;
}
