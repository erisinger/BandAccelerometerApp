package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by erikrisinger on 6/14/16.
 */
public class MHLGyroscopeReading extends MHLSensorReading {

    private double x, y, z;
    private long timestamp;

    public MHLGyroscopeReading(int userID, String deviceType, String data){
        super(userID, deviceType, "SENSORY_GYRO");

        String[] splitData = data.split(",");
        this.timestamp = Long.parseLong(splitData[0]);
        this.x = Double.parseDouble(splitData[1]);
        this.y = Double.parseDouble(splitData[2]);
        this.z = Double.parseDouble(splitData[3]);
    }

    @Override
    public JSONObject toJSONObject(){
        JSONObject data = new JSONObject();
        JSONObject obj = new JSONObject();

        try {
            data.put("t", timestamp);
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);

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
