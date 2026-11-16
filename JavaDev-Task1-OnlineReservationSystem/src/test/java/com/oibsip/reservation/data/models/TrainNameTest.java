package com.oibsip.reservation.data.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainNameTest {

    @ParameterizedTest
    @CsvSource({
            "1001, CLASSIC",
            "1500, CLASSIC",
            "2000, CLASSIC",
            "2001, PREMIUM",
            "2500, PREMIUM",
            "3000, PREMIUM",
            "3001, LOGISTICS",
            "3500, LOGISTICS",
            "4000, LOGISTICS"
    })
    void findByTrainNumber_returnsTheTrainOwningThatRange(long trainNumber, TrainName expected) {
        assertEquals(expected, TrainName.findByTrainNumber(trainNumber));
    }

    @ParameterizedTest
    @ValueSource(longs = {Long.MIN_VALUE, -1, 0, 1, 1000, 4001, 9999, Long.MAX_VALUE})
    void findByTrainNumber_returnsNullOutsideEveryRange(long trainNumber) {
        assertNull(TrainName.findByTrainNumber(trainNumber));
    }

    @Test
    void everyNumberFrom1001To4000BelongsToExactlyOneTrain() {
        for (long number = 1001; number <= 4000; number++) {
            assertNotNull(TrainName.findByTrainNumber(number), "No train for " + number);
        }
    }

    @Test
    void rangesAreExposedThroughGetters() {
        assertEquals(1001, TrainName.CLASSIC.getStartNumber());
        assertEquals(2000, TrainName.CLASSIC.getEndNumber());
        assertEquals(2001, TrainName.PREMIUM.getStartNumber());
        assertEquals(3000, TrainName.PREMIUM.getEndNumber());
        assertEquals(3001, TrainName.LOGISTICS.getStartNumber());
        assertEquals(4000, TrainName.LOGISTICS.getEndNumber());
    }
}
