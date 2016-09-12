package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erikrisinger on 6/15/16.
 */
public class MHLRSSIReading extends MHLSensorReading {

    private long timestamp;
    private int rssi;

    public MHLRSSIReading(String userID, String deviceType, String deviceID, String data, String label){
        super(userID, deviceType, deviceID, "SENSOR_RSSI", label);

        String[] splitData = data.split(",");
        this.timestamp = Long.parseLong(splitData[0]);
        this.rssi = Integer.parseInt(splitData[1]);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject data = new JSONObject();
        JSONObject device = new JSONObject();
        JSONObject obj = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("rssi", rssi);

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
