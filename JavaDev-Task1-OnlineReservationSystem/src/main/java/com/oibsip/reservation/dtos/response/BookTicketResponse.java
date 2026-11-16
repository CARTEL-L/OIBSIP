package com.oibsip.reservation.dtos.response;

import com.oibsip.reservation.data.models.Station;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookTicketResponse {

    private LocalDate dateOfJourney;
    private long trainNumber;
    private Station destinationStation;
    private String message;
    private String PNR;

}
