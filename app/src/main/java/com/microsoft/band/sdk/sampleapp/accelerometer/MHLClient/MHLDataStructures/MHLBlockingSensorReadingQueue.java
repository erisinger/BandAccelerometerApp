package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLDataStructures;

import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLSensorReading;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by erikrisinger on 6/14/16.
 */
public class MHLBlockingSensorReadingQueue extends ArrayBlockingQueue<MHLSensorReading> {
    private static int QUEUE_SIZE = 5000;
    public MHLBlockingSensorReadingQueue(){
        super(QUEUE_SIZE);
    }
}