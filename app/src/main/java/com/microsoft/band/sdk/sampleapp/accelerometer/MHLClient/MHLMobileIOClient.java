package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.band.sdk.sampleapp.accelerometer.BandAccelerometerAppActivity;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLDataStructures.MHLBlockingSensorReadingQueue;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLSensorReading;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by erikrisinger on 6/14/16.
 */
public class MHLMobileIOClient {

    private BandAccelerometerAppActivity parentActivity;
    private MHLBlockingSensorReadingQueue sensorReadingQueue;
    private Thread clientThread = null;
    private String ip;
    private int port;
    private String userID;
    private boolean started = false;

//    //GET RID OF THIS!
//    private ArrayList<MHLSensorReading> sensorReadingArrayList;

    //constructor for pre-existing (external) queue
    public MHLMobileIOClient(BandAccelerometerAppActivity a, MHLBlockingSensorReadingQueue q, String ip, int port, String id){
        this.parentActivity = a;
        this.sensorReadingQueue = q;
        this.ip = ip;
        this.port = port;
        this.userID = id;
        this.startClient();
    }

    //constructor for self-contained queue
    public MHLMobileIOClient(BandAccelerometerAppActivity a, String ip, int port, String id){
        this.parentActivity = a;
        this.sensorReadingQueue = new MHLBlockingSensorReadingQueue();
        this.ip = ip;
        this.port = port;
        this.userID = id;
        this.startClient();
    }

    //in the future, this may be set to public and explicitly called by the user
    private void startClient(){
        if (started) return;

//        //GET RID OF THIS!!
//        sensorReadingArrayList = new ArrayList<MHLSensorReading>();

        started = true;
        clientThread = new Thread(new MHLClientThread(ip, port, userID));
        clientThread.start();
    }

    public boolean addSensorReading(MHLSensorReading reading){
        if (!started) this.startClient();

        return sensorReadingQueue.offer(reading);
    }

    class MHLClientThread implements Runnable {
        String ip;
        int port;
        String userID;

        public MHLClientThread(String ip, int port, String id){
            this.ip = ip;
            this.port = port;
            this.userID = id;
        }

        @Override
        public void run() {
            Socket socket = null;
            MHLTransmissionThread transmissionThread = null;
            MHLConsumptionThread consumptionThread = null;

            System.out.println("STARTING ACCEL THREAD");

            try {
                System.out.println("connecting to server: " + ip + ":" + port);
                socket = new Socket(ip, port);
                System.out.println("connected");
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String handshake = input.readLine();

                if (handshake == null || !"ID".equals(handshake)){
                    System.out.println("handshake failed with string " + handshake);
                    return;
                }

                System.out.println("handshake: " + handshake);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            if (socket == null){
                System.out.println("null socket: exiting");
                return;
            }

            //connection successful -- launch transmission thread
            transmissionThread = new MHLTransmissionThread(socket);
            new Thread(transmissionThread).start();

            //launch notification consumption thread
            consumptionThread = new MHLConsumptionThread(socket);
            new Thread(consumptionThread).start();
        }
    }

    class MHLTransmissionThread implements Runnable {
        private Socket clientSocket;
        private BufferedWriter output;
        private BufferedReader input;

        private boolean running = false;

        public MHLTransmissionThread(Socket clientSocket){
            this.clientSocket = clientSocket;

            try {
                this.output = new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream()));
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        public void run(){
            ArrayList<MHLSensorReading> latestReadings;

            //connect to data collection server (DCS), return on failed handshake
            System.out.println("calling connectToServer()");
            this.connectToServer();
//            System.out.println("called connectToServer()");
//            if (!running) return;

            //transmit data continuously until stopped
            while (!Thread.currentThread().isInterrupted()){
                //auto reconnect in case of interruption
//                if (!running) this.connectToServer();
                try {


                    latestReadings = new ArrayList<MHLSensorReading>();
                    sensorReadingQueue.drainTo(latestReadings);

                    MHLSensorReading reading;
                    for (int i = latestReadings.size() - 1; i >= 0; i--){
                        reading = latestReadings.get(i);

                        output.write(reading.toJSONString() + "\n");
                        output.flush();
//                        Log.d("reading: ", reading.toJSONString());
//                    }
                    }

                    Thread.sleep(10);
                } catch (IOException e){
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
//            running = false;
        }
        private void connectToServer(){
            try {

//                String handshake = input.readLine();
//
//                if (!"ID".equals(handshake)){
//                    System.out.println("handshake failed with string " + handshake);
//                    return;
//                }

                //send user ID
                System.out.println("sending ID");
                output.write("ID," + userID + "\n");
                output.flush();

                //read in ACK
                String ackString = input.readLine();
                String[] ack = ackString.split(",");

                Log.d("ACK: ", ackString);

                parentActivity.appendToUI("received ACK from server: " + ackString);

                //expecting "ACK" with user ID echoed back as CSV string, e.g.: "ACK,0"
                if (!("ACK".equals(ack[0]) && ack[1].equals(userID))){
                    Log.d("error: ", "failed to receive correct ACK from DCS");
                }
                else {
                    running = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MHLConsumptionThread implements Runnable {
        private Socket socket;
        private BufferedReader input;

        public MHLConsumptionThread(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try {

                String inputLine;
                this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

                while ((inputLine = input.readLine()) != null){
                    Log.d("received notification: ", inputLine);



                    JSONObject message = new JSONObject(inputLine);
                    if (message.get("sensor_type").equals("SENSOR_SERVER_MESSAGE") && message.get("message_type").equals("USER_NOTIFICATION")) {
                        String uiMessage = (String)message.get("message");
                        parentActivity.appendToUI(uiMessage);


                        //TO DO: create alert dialog here
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
