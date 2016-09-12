package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erikrisinger on 6/14/16.
 */
public class MHLGyroscopeReading extends MHLSensorReading {

    private double x, y, z;
    private long timestamp;

    public MHLGyroscopeReading(String userID, String deviceType, String deviceID, long t, double x, double y, double z, String label){
        super(userID, deviceType, deviceID, "SENSOR_GYRO", label);
        this.timestamp = t;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public JSONObject toJSONObject(){
        JSONObject data = new JSONObject();
        JSONObject device = new JSONObject();
        JSONObject obj = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);

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
