package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings;

import org.json.JSONObject;

/**
 * Created by erikrisinger on 6/14/16.
 */
public abstract class MHLSensorReading {

    protected String userID;
    protected String deviceType;
    protected String deviceID;
    protected String sensorType;
    protected String label;

    public MHLSensorReading(String userID, String deviceType, String deviceID, String sensorType, String label){
        this.userID = userID;
        this.deviceType = deviceType;
        this.deviceID = deviceID;
        this.sensorType = sensorType;
        this.label = label;
    }

    public String getUserID(){
        return userID;
    }

    public String getDeviceType(){
        return deviceType;
    }

    public String getDeviceID() { return  deviceID; }

    public String getSensorType(){
        return sensorType;
    }

    public String getLabel() { return label; }

    public abstract JSONObject toJSONObject();

    public String toJSONString(){
        return this.toJSONObject().toString();
    }
}
