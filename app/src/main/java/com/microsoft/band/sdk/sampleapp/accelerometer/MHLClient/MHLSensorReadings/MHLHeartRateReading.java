package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erikrisinger on 6/23/16.
 */
public class MHLHeartRateReading extends MHLSensorReading {
    private long timestamp;
    private int heartRate;

    public MHLHeartRateReading(String userID, String deviceType, String deviceID, long t, int rate, String label){
        super(userID, deviceType, deviceID, "SENSOR_HEART_RATE", label);

        this.timestamp = t;
        this.heartRate = rate;
    }

    @Override
    public JSONObject toJSONObject(){
        JSONObject data = new JSONObject();
        JSONObject device = new JSONObject();
        JSONObject obj = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("heartRate", heartRate);

            device.put("device_id", deviceID);
            device.put("device_type", deviceType);

            obj.put("user_id", userID);
            obj.put("device_type", deviceType);
            obj.put("device", device);
            obj.put("sensor_type", sensorType);
            obj.put("data", data);
            obj.put("label", label);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }
}
