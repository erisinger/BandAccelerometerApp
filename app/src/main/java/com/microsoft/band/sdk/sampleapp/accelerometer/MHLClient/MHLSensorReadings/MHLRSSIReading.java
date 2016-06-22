package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erikrisinger on 6/15/16.
 */
public class MHLRSSIReading extends MHLSensorReading {

    private long timestamp;
    private int rssi;

    public MHLRSSIReading(int userID, String deviceType, String data){
        super(userID, deviceType, "SENSOR_RSSI");

        String[] splitData = data.split(",");
        this.timestamp = Long.parseLong(splitData[0]);
        this.rssi = Integer.parseInt(splitData[1]);
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject data = new JSONObject();
        JSONObject obj = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("rssi", rssi);

            obj.put("user_id", userID);
            obj.put("device_type", deviceType);
            obj.put("sensor_type", sensorType);
            obj.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
