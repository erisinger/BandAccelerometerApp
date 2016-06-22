package com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient;

import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLDataStructures.MHLBlockingSensorReadingQueue;
import com.microsoft.band.sdk.sampleapp.accelerometer.MHLClient.MHLSensorReadings.MHLSensorReading;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by erikrisinger on 6/14/16.
 */
public class MHLMobileIOClient {

    private MHLBlockingSensorReadingQueue sensorReadingQueue;
    private Thread clientThread = null;
    private String ip;
    private int port;
    private int userID;
    private boolean started = false;

    //constructor for pre-existing (external) queue
    public MHLMobileIOClient(MHLBlockingSensorReadingQueue q, String ip, int port, int id){
        this.sensorReadingQueue = q;
        this.ip = ip;
        this.port = port;
        this.userID = id;
        this.startClient();
    }

    //constructor for self-contained queue
    public MHLMobileIOClient(String ip, int port, int id){
        this.sensorReadingQueue = new MHLBlockingSensorReadingQueue();
        this.ip = ip;
        this.port = port;
        this.userID = id;
        this.startClient();
    }

    //in the future, this may be set to public and explicitly called by the user
    private void startClient(){
        if (started) return;

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
        int userID;

        public MHLClientThread(String ip, int port, int id){
            this.ip = ip;
            this.port = port;
            this.userID = id;
        }

        @Override
        public void run() {
            Socket socket = null;
            MHLTransmissionThread transmissionThread = null;

            System.out.println("STARTING ACCEL THREAD");

            try {
                System.out.println("connecting to server: " + ip + ":" + port);
                socket = new Socket(ip, port);
                System.out.println("connected");
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
//            System.out.println("calling connectToServer()");
            this.connectToServer();
//            System.out.println("called connectToServer()");
//            if (!running) return;

            //transmit data continuously until stopped
            while (!Thread.currentThread().isInterrupted()){
                //auto reconnect in case of interruption
//                if (!running) this.connectToServer();
//                System.out.println("inside while");
                try {
                    latestReadings = new ArrayList<MHLSensorReading>();
                    sensorReadingQueue.drainTo(latestReadings);

                    for (int i = latestReadings.size() - 1; i >= 0; i--){
                        MHLSensorReading reading = latestReadings.get(i);
                        output.write(reading.toJSONString() + "\n");
                        output.flush();
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

                System.out.println("connectToServer()");

                //send user ID
                output.write("ID," + userID + "\n");
                output.flush();

                //read in ACK
                String ackString = input.readLine();
                String[] ack = ackString.split(",");

                System.out.println(ackString);

                //expecting "ACK" with user ID echoed back as CSV string, e.g.: "ACK,0"
                if (!("ACK".equals(ack[0]) && Integer.parseInt(ack[1]) == userID)){
                    System.out.println("failed to receive correct ACK from DCS");
                }
                else {
                    running = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
