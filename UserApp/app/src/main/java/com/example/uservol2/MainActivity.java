package com.example.uservol2;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.Manifest;


public class MainActivity extends AppCompatActivity {
    private double latitude, longitude;
    private String xmlFileName;
    private String[] xlat, ylong;
    private String ipAddress;
    private int portNumber, sendingDuration;
    String csvData;
    private Button btStop, connectButton;
    private Switch mode;
    private TextView textView;
    private int device_id = 3;
    private MqttAndroidClient mqttAndroidClient;
    private FusedLocationProviderClient location_client;
    private boolean isChecked;
    private int current_timestep=0;
    private boolean hasInitialized=false;
    private int dangerCounter=0;



    /* ===================  FOR THE MENU  ======================= */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    //Handler -> When the user clicks one of the menu options
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int itemId = item.getItemId();

        if (itemId == R.id.ip) {
            //Show IP input dialog
            showIpInputDialog();
            return true;
        } else if (itemId == R.id.port) {
            //Show Port Number input dialog
            showPortInputDialog();
            return true;
        } else if (itemId == R.id.timer) {
            //Show Sending Duration input dialog
            showTimerInputDialog();
            return true;
        } else if (itemId == R.id.exit) {
            //Show Exiting Screen
            showExitConfirmationDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //IP menu option
    private void showIpInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter IP Address");

        //Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        //Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ipAddress = input.getText().toString();
                //Write a Toast with the entered IP
                Toast.makeText(MainActivity.this, "Entered IP: " + ipAddress, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //Port number menu option
    private void showPortInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Port Number");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    portNumber = Integer.parseInt(input.getText().toString());
                    //Write a Toast with the entered Port
                    Toast.makeText(MainActivity.this, "Entered Port: " + portNumber, Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    //The case where the entered text is not a valid integer
                    Toast.makeText(MainActivity.this, "Invalid Port Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //Sending Duration menu option
    private void showTimerInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Sending Duration");

        // Set up the input
        final EditText input = new EditText(this);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    sendingDuration = Integer.parseInt(input.getText().toString());
                    //Write a Toast with the entered Timer Duration
                    Toast.makeText(MainActivity.this, "Entered Sending Duration: " + sendingDuration, Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    //The case where the entered text is not a valid integer
                    Toast.makeText(MainActivity.this, "Invalid Sending Duration", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //Exit menu option
    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Are you sure you want to exit the application?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //If the user clicks "Yes" exit the application
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //If the user clicks "No" dismiss the dialog
                dialog.dismiss();
            }
        });

        builder.show();
    }
    /* ========================================================== */

    //Setters used for the fetchLocation()
    public void setGlobalLong(double longt) {
        longitude = longt;
    }
    public void setGlobalLat(double lat) {
        latitude = lat;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!hasInitialized){
            hasInitialized=true;
            xmlFileName = XmlSelect.getXmlName();
            parseXml();
        }

        //Used for the fetchLocation()
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        location_client = LocationServices.getFusedLocationProviderClient(this);

        /* === Set the Toolbar as the ActionBar === */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /* ======================================== */


        /* ======== The Connect Button for the broker ======== */
        btStop = findViewById(R.id.stop);

        connectButton = findViewById(R.id.connect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Connect
                mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "tcp://" + ipAddress + ":" + portNumber, MqttClient.generateClientId());
                mqttAndroidClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        Log.d("testing", "connection lost");
                    }

                    //Handling the messages from the Server and giving an audiovisual warning
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        //Convert MQTT message to string
                        String receivedMessage = new String(message.getPayload());

                        //Check if the message starts with "0,"
                        if (!receivedMessage.startsWith("0,")) {
                            //Update TextView with the received message
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(receivedMessage.startsWith("H") || receivedMessage.startsWith("M")){
                                        textView.setText(receivedMessage);
                                        dangerCounter=5;
                                    }
                                    else {
                                        //Reset TextView to not display any message
                                        textView.setText("");
                                    }

                                    //Only at Moderate/High Danger play audio
                                    if (receivedMessage.startsWith("H") || receivedMessage.startsWith("M")) {
                                        //Play sound based on the message
                                        playSound(receivedMessage);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        Log.d("testing", "msg delivered");
                    }
                });
                try {
                    MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                    mqttConnectOptions.setCleanSession(true);
                    mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.d("testing", "connect succeed");
                            subscribe("android3");
                            btStop.setVisibility(View.VISIBLE);
                            btStop.setClickable(true);
                            connectButton.setVisibility(View.INVISIBLE);
                            connectButton.setClickable(false);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.d("testing", "connect failed");
                            exception.printStackTrace();
                        }
                    });

                    //sendMessage("iot1","papies");
                } catch (MqttException e) {
                    Log.d("Testing", "Failed to connect : " + e.getMessage());

                }
            }
        });
        /* =================================================== */


        /* ======== The Switch functionality ======== */
        //Initialize the switch
        mode = findViewById(R.id.modeSwitch);

        //Set a listener for the switch change events
        mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MainActivity.this.isChecked = isChecked;
            }
        });
        /* ========================================== */


        /* ======== The STOP Button functionality ======== */
        //Initialize the stop button
        btStop.setVisibility(View.INVISIBLE);
        btStop.setClickable(false);
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Disconnect when the user presses the stop button
                try {
                    mqttAndroidClient.unregisterResources();
                    mqttAndroidClient.close();
                    mqttAndroidClient.disconnect();
                    mqttAndroidClient.setCallback(null);
                    mqttAndroidClient = null;
                    btStop.setVisibility(View.INVISIBLE);
                    btStop.setClickable(false);
                    connectButton.setVisibility(View.VISIBLE);
                    connectButton.setClickable(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        /* =============================================== */





        // Find the TextView by its ID
        textView = findViewById(R.id.textView);

        /* The core of the program */
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //Reset TextView to not display any message
                if(dangerCounter==0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView = findViewById(R.id.textView);
                            textView.setText("");
                        }
                    });
                }
                else{
                    dangerCounter--;
                }

                if(isChecked==false) {
                    fetchLocation();
                    Log.d("vik", latitude + " " + longitude + " --- " + isChecked);
                    String st = "0," + latitude + "," + longitude + "," + device_id;
                    publishMessage(st);
                }
                else if (isChecked==true) {

                    String[] timesteps = csvData.split("\n");

                    String st = "0," + timesteps[current_timestep] + "," + device_id;
                    publishMessage(st);
                    current_timestep++;

                    //Case with no sending duration
                    if(sendingDuration==0) {
                        if (current_timestep >= timesteps.length) {
                            current_timestep = 0;
                        }
                    }
                    //Case with sending duration
                    else{
                        if(sendingDuration>timesteps.length){
                            sendingDuration=timesteps.length;
                        }
                        if(current_timestep >= sendingDuration){
                            current_timestep = 0;
                        }
                    }
                }
            }
        }, 0, 1000);





        /* ===== Periodic checks on whether there is a Internet connection =====*/
        Handler handler = new Handler();
        Runnable checkConnectivity = new Runnable() {
            @Override
            public void run() {
                //Get the system's connectivity manager
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                //Check if the device is connected to the internet using NetworkCapabilities
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                if (isConnected) {
                    //Device is connected to the internet
                    showToast("Device is connected to the internet");
                } else {
                    //Device is not connected to the internet -> Give solution to user via Settings
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("No Internet Connection");
                    builder.setMessage("Please activate your internet connection to use this app.");
                    builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Open the device's settings where the user can activate their internet connection
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                }

                // Schedule the Runnable to run again in 30 seconds
                handler.postDelayed(this, 30000);
            }
        };

        //Schedule the Runnable to run for the first time immediately
        handler.post(checkConnectivity);
        /* ========================================================= */


    }




    /* Used for showing a toast message for the periodic Internet connection checker */
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    /* Used for finding the users current position */
    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            Log.d("GPS","GPS has permisiom");
            location_client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    Log.d("GPS","on success");
                    if (location != null) {
                        setGlobalLong(location.getLongitude());
                        setGlobalLat(location.getLatitude());
                        Log.d("GPS","found");
                    }
                }
            });
        }
    }


    /* Parse the xml file, store the values into arrays and turn it into a csv file */
    private void parseXml() {
        try {
            InputStream is = getAssets().open(xmlFileName);                          //Load the XML file from the assets folder
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); //Create document builder factory
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();               //Create document builder

            Document doc = dBuilder.parse(is);                              //Parse the xml file
            NodeList nodeList = doc.getElementsByTagName("timestep");       //Get the node list of "timestep" elements

            //Store the x & y in the arrays
            xlat = new String[nodeList.getLength()];
            ylong = new String[nodeList.getLength()];


            //Store the values into the arrays
            for(int i=0; i<nodeList.getLength(); i++){
                Element timestepElement = (Element) nodeList.item(i);

                String x = timestepElement.getElementsByTagName("vehicle").item(0).getAttributes().getNamedItem("x").getNodeValue();
                xlat[i] = x;
                String y = timestepElement.getElementsByTagName("vehicle").item(0).getAttributes().getNamedItem("y").getNodeValue();
                ylong[i] = y;

                //After parsing, call the method to convert to CSV
                csvData = convertToCsv(ylong, xlat);  //1st ylong and 2nd xlat -> cause the given data have wrong layout
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* HELPFUL FUNCTION: For turning a xml file into csv */
    private String convertToCsv(String[] xArray, String[] yArray) {
        StringBuilder csvContent = new StringBuilder();

        //Add data rows
        for(int j = 0; j < xArray.length; j++) {
            csvContent.append(xArray[j]).append(",").append(yArray[j]).append("\n");
        }

        //Return the CSV representation
        return csvContent.toString();
    }


    /* Used for publishing the messages */
    public void publishMessage(String payload) {
        try {
            if(mqttAndroidClient!=null){
                if (mqttAndroidClient.isConnected() == false) {
                    return;
                }
            }
            else{
                return;
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish("android3", message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("testing", "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("testing", "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e("testing", e.toString());
            e.printStackTrace();
        }
    }


    /* Used for subscribing at a topic */
    public void subscribe(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /* Plays the appropriate sound based on the message */
    private void playSound(String message) {
        int soundResourceId = R.raw.mid;
        if (message.startsWith("M")) {
            soundResourceId = R.raw.mid;
        }
        else if (message.startsWith("H")) {
            soundResourceId = R.raw.high;
        }

        // Create MediaPlayer instance and play the selected sound
        MediaPlayer mediaPlayer = MediaPlayer.create(this, soundResourceId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //Release MediaPlayer resources after completion
                mp.release();
            }
        });
        mediaPlayer.start();
    }

}