
package com.oibsip.reservation.data.models;

public enum TrainName {

    CLASSIC(1001, 2000),
    PREMIUM(2001, 3000),
    LOGISTICS(3001, 4000);

    private final long startNumber;
    private final long endNumber;

    TrainName(long startNumber, long endNumber) {
        this.startNumber = startNumber;
        this.endNumber = endNumber;
    }

    public long getStartNumber() {
        return startNumber;
    }

    public long getEndNumber() {
        return endNumber;
    }

    public static TrainName findByTrainNumber(long trainNumber) {

        for (TrainName trainName : TrainName.values()) {

            if (trainNumber >= trainName.startNumber
                    && trainNumber <= trainName.endNumber) {

                return trainName;
            }
        }

        return null;
    }
}
