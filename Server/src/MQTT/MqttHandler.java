package MQTT;

import static Distance.Distance.calculate;

import Database.Database;
import org.eclipse.paho.client.mqttv3.*;
import Distance.Distance;
import Maps.Maps;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;


public class MqttHandler implements MqttCallback {

    public boolean android_flag=false, iot1_flag=false, iot2_flag=false;
    private double device_id;
    private double Danger;

    double android_lon, android_lat, iot1_lon, iot1_lat, iot2_lon, iot2_lat;

    double android_id, iot1_id, iot2_id;
    Distance.Point android_marker, iot1_marker, iot2_marker;

    private MqttClient client;

    private final String serverUrl;

    Database dbController;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private static Map<String, Double> dangerThresholds = new HashMap<>();
    static {
        //Thresholds for every IOT
        dangerThresholds.put("smoke", 0.14);
        dangerThresholds.put("gas",  9.15);
        dangerThresholds.put("temperature", 50.0);
        dangerThresholds.put("uv", 6.0);
    }

    String Message;

    public MqttHandler(String url, Database db)
    {
        this.serverUrl = url;
        this.dbController = db;
    }

    public void Connect()
    {
        String topic1 = "iot1";
        String topic2 = "iot2";
        String topic3 = "android3";
        String clientId = "subscribe_client";
        try {
            this.client = new MqttClient(serverUrl, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);//this.client = new MqttClient(serverUrl, MqttClient.generateClientId(), new MqttDefaultFilePersistence("/tmp"));
            System.out.println("Connecting to server with URL: " + serverUrl + " with clientId: " + client.getClientId());
            client.connect(connOpts);

            this.client.setCallback(this);
            this.client.subscribe(topic1, 0);
            this.client.subscribe(topic2, 0);
            this.client.subscribe(topic3, 0);

        } catch (MqttException e) {
            System.out.println("Server not connected to broker with url: " + serverUrl);
            e.printStackTrace();
        }
    }

    public void sendMessage(String topic, String message) throws MqttException {
//        System.out.println("Topic: " + topic + "Arrived Message: " + message);
        MqttMessage msg = new MqttMessage();
        msg.setPayload(message.getBytes());
        client.publish(topic, msg);
    }

    public void disconnect() throws MqttException
    {
        try {
            this.client.disconnect();
        } catch (MqttException e) {
            System.out.println("Failed to disconnect.");
            e.printStackTrace();
        }
    }


    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub
        System.out.println("Connection lost: " + cause.getMessage());
    }

    private String calculateDangerLevel(double smoke, double gas, double temperature, double uv) {
        if (smoke > dangerThresholds.get("smoke") && gas > dangerThresholds.get("gas")) {
            this.Danger = 1;
            return "High Danger";
        } else if (temperature > dangerThresholds.get("temperature") && uv > dangerThresholds.get("uv")) {
            this.Danger = 1;
            return "Moderate Danger";
        } else if (gas > dangerThresholds.get("gas")) {
            this.Danger = 1;
            return "High Danger";
        } else if (smoke > dangerThresholds.get("smoke") && gas > dangerThresholds.get("gas")
                && temperature > dangerThresholds.get("temperature") && uv > dangerThresholds.get("uv")) {
            this.Danger = 1;
            return "High Danger";
        } else {
            return "Low Danger";
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws MqttException, ParseException {
        Message = message.toString();

        if (Message.startsWith("H") || Message.startsWith("M") ||Message.startsWith("L"))
        {
           System.out.println("EXIT");
            return;
        }

        System.out.println("Topic: " + topic + "Arrived Message: " + message);
        Message = Message.trim();

        String[] fields = Message.split(",");
        String type = fields[0];
        int typeVal = Integer.parseInt(type);
        if (typeVal == 0){
            //if android device
            android_flag=true;
            String and_lat = fields[1];
            String and_lon = fields[2];
            String device_id = fields[3];
            android_lat = Double.parseDouble(and_lat);
            android_lon = Double.parseDouble(and_lon);
            android_id = Double.parseDouble(device_id);
            android_marker = new Distance.Point(android_lat, android_lon);

            //Add marker for android
            Maps.addAndroidpoint(android_id, android_lat, android_lon);

        }
        else{
            // iot devices
            String Smoke = fields[4];
            String Gas = fields[5];
            String Temperature = fields[6];
            String UV = fields[7];
            String Battery = fields[8];
            double SmokeVal = Double.parseDouble(Smoke);
            double GasVal = Double.parseDouble(Gas);
            double TemperatureVal = Double.parseDouble(Temperature);
            double UVVal = Double.parseDouble(UV);
            double Batteryval = Double.parseDouble(Battery);


            //Check all the sensors
            String result = calculateDangerLevel(SmokeVal, GasVal,TemperatureVal,UVVal);

            //check flags
            if ((result.equals("High Danger")) || (result.equals("Moderate Danger"))){
                if (typeVal == 1){ //first iot device
                    iot1_flag=true;
                }else{ //second iot device
                    iot2_flag=true;
                }
            }else{ //low danger
                if (typeVal == 1){ //first iot device
                    iot1_flag=false;
                }else{ //second iot device
                    iot2_flag=false;
                }
            }

            if (typeVal == 1) //first iot
            {
                String iot_lat = fields[1];
                String iot_lon = fields[2];
                String device_id = fields[3];

                iot1_lat = Double.parseDouble(iot_lat);
                iot1_lon = Double.parseDouble(iot_lon);
                iot1_id = Double.parseDouble(device_id);

                iot1_marker = new Distance.Point(iot1_lat, iot1_lon);
                System.out.println("iot2_flag "+ iot2_flag);
                Maps.addIoTpoint(iot1_id,iot1_lat, iot1_lon, iot2_flag,Batteryval, SmokeVal, GasVal, TemperatureVal, UVVal);
            }
            else
            {
                String iot_lat = fields[1];
                String iot_lon = fields[2];
                iot2_lat = Double.parseDouble(iot_lat);
                iot2_lon = Double.parseDouble(iot_lon);
                System.out.println("iot1_flag "+ iot1_flag);
                Maps.addIoTpoint2(iot2_id,iot2_lat, iot2_lon,iot1_flag, Batteryval, SmokeVal, GasVal, TemperatureVal, UVVal);
            }

            //calculate remaining variables
            if(((result.equals("High Danger")) || (result.equals("Moderate Danger"))) && (android_flag==true)){
                System.out.println("1");
                if(typeVal == 1){ //first iot device
                    System.out.println("2");
                    if(iot2_flag == false){//no iot2
                        System.out.println("3");
                        //Send data to Android
                        double dist = calculate(android_marker,iot1_marker, null);
                        String SendDist = String.valueOf(dist);
                        String[] sent = new String[]{result, SendDist};
                        String toSentArray = String.join(",", sent);
                        sendMessage("android3", toSentArray);

                        //Get current time
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        System.out.println(dateFormat.format(timestamp));
                        String time = dateFormat.format(timestamp);

                        //Add to the database
                        this.dbController.InsertDB(time, android_id, iot1_lat, iot1_lon, SmokeVal, GasVal, TemperatureVal, UVVal, this.Danger);

                    }else{

                        //if we have iot2
                        double dist = calculate(android_marker,iot1_marker, iot2_marker);
                        String SendDist = String.valueOf(dist);
                        String[] sent = new String[]{result, SendDist};
                        String toSentArray = String.join(",", sent);
                        sendMessage("android3", toSentArray);

                        //Get current time
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        //System.out.println(dateFormat.format(timestamp));
                        String time = dateFormat.format(timestamp);

                        //Add to the database
                        this.dbController.InsertDB(time, android_id, iot2_lat, iot2_lon, SmokeVal, GasVal, TemperatureVal, UVVal, this.Danger);
                    }
                }else if (typeVal == 2){
                    if(iot1_flag == false){//no iot1

                        //Send data to Android
                        double dist = calculate(android_marker,iot2_marker, null);
                        String SendDist = String.valueOf(dist);
                        String[] sent = new String[]{result, SendDist};
                        String toSentArray = String.join(",", sent);
                        sendMessage("android3", toSentArray);

                        //Get current time
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        //System.out.println(dateFormat.format(timestamp));
                        String time = dateFormat.format(timestamp);

                        //Add to the database
                        this.dbController.InsertDB(time, device_id, iot2_lat, iot2_lon, SmokeVal, GasVal, TemperatureVal, UVVal, this.Danger);


                    }else{

                        //if we have iot1
                        double dist = calculate(android_marker,iot1_marker, iot2_marker);
                        String SendDist = String.valueOf(dist);
                        String[] sent = new String[]{result, SendDist};
                        String toSentArray = String.join(",", sent);
                        sendMessage("android3", toSentArray);

                        //Get current time
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        //System.out.println(dateFormat.format(timestamp));
                        String time = dateFormat.format(timestamp);

                        //Add to the database
                        this.dbController.InsertDB(time, android_id, iot1_lat, iot1_lon, SmokeVal, GasVal, TemperatureVal, UVVal, this.Danger);
                    }
                }
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //TODO Auto-generated method stub
    }

}