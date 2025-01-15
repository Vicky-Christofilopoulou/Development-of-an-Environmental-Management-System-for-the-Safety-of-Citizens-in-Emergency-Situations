package com.example.iotdevice2pahotest;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.graphics.Color.rgb;
import android.content.pm.PackageManager;

import android.location.Location;



import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.View;

import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.android.material.tabs.TabLayout;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient mqttAndroidClient;
    private FusedLocationProviderClient location_client;

    private boolean recording =false;
    private static List<Sensor> sensor_list=new ArrayList<Sensor>();
    private TextView sensor_type_text_view;
    private Slider value_slider_object;
    private CheckBox sensor_enabled;
    private int currentTab=0;
    private static double global_long=0;
    private static double global_lat=0;
    private final double[] default_lats = {37.96809452684323, 37.96799937191987, 37.967779456380754, 37.96790421900921};
    private final double[] default_longs= {23.76630586399502, 23.766603589104385, 23.767174897611685, 23.76626294807113};
    private static double default_long=37.96809452684323;
    private static double default_lat=23.76630586399502;
    private static boolean GPS_availability=false;
    private Slider.OnChangeListener slider_on_change_listener=null;
    private static boolean hasStarted=false;
    private static float batteryPct=0;
    private static String server_ip="192.168.1.78";
    private static int server_port=2028;
    private static boolean is_manual=false;
    private static boolean is_iot_1=true;

    public static void addSensor(Sensor s){
        sensor_list.add(s);
    }
    public static void setIsManual(boolean is_manual_new){
        is_manual=is_manual_new;
    }
    public static boolean getIsManual(){
        return is_manual;
    }
    public static void setIsIoT1(boolean is_iot_1_new){
        is_iot_1=is_iot_1_new;
    }
    public static boolean getIsIoT1(){
        return is_iot_1;
    }
    public static void setServerPort(int server_port_new){
        server_port=server_port_new;
    }
    public static int getServerPort(){
        return server_port;
    }
    public static void setServerIp(String server_ip_new){
        server_ip=server_ip_new;
    }
    public static String getServerIp(){
        return server_ip;
    }
    public static void setGlobalLong(double longt){global_long=longt;}
    public static void setGlobalLat(double lat){global_lat=lat;}
    public static void setGPSAvailability(boolean avail){GPS_availability=avail;}
    public static boolean getGPSAvailability(){return GPS_availability;};
    private ImageButton settings_button;
    private Button recording_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        startupPositionRandomization();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionRequest();
        setSettingsButtonFunctionality();
        setRecordingButtonFunctionnality();
        Log.d("Testing","onCreate()");
        startupSensorInitialization();
        TimerInitialization();
        managingSensorTabLayout();

    }
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            batteryPct = level * 100 / (float)scale;

        }
    };
    public void publishMessage(String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            java.lang.String topic="iot1";
            if(!getIsIoT1()){
                topic="iot2";
            }
            mqttAndroidClient.publish(topic, message,null, new IMqttActionListener() {
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
    private void startupPositionRandomization(){
        if(!hasStarted){
            hasStarted=true;
            int pos_id=(int) (Math.random() * 3);
            default_long=default_longs[pos_id];
            default_lat=default_lats[pos_id];

        }

    }
    private void startupSensorInitialization(){
        if(sensor_list.size()==0){
            sensor_list.add(new Sensor(type.SMOKE,0,0.25f));
            sensor_list.add(new Sensor(type.TEMPERATURE,-4,80));
            sensor_list.get(0).setValue(0.1f);
            sensor_list.get(1).setValue(10);
        }
    }
    private void permissionRequest(){
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
        location_client = LocationServices.getFusedLocationProviderClient(this);
    }
    private void setSettingsButtonFunctionality(){
        settings_button = (ImageButton)findViewById(R.id.settings_icon);
        settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void setRecordingButtonFunctionnality(){
        recording_button = (Button)findViewById(R.id.start_broadcast);
        recording_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!recording){

                    mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), "tcp://"+server_ip+":"+String.valueOf(server_port), MqttClient.generateClientId());
                    mqttAndroidClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Log.d("testing", "connection lost");
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.d("testing", "topic: " + topic + ", msg: " + new String(message.getPayload()));
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
                                recording=true;
                                recording_button.setBackgroundColor(rgb(217, 13, 13));
                                recording_button.setText("STOP SENDING");

                                settings_button.setVisibility(View.INVISIBLE);
                                settings_button.setClickable(false);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.d("testing", "connect failed");
                            }
                        });


                        //sendMessage("iot1","papies");
                    } catch (MqttException e) {
                        Log.d("Testing","Failed to connect : "+e.getMessage());

                    }
                }
                else{
                    recording=false;
                    recording_button.setBackgroundColor(rgb(41, 152, 0));
                    recording_button.setText("START SENDING");
                    settings_button.setVisibility(View.VISIBLE);
                    settings_button.setClickable(true);
                    try {
                        mqttAndroidClient.unregisterResources();
                        mqttAndroidClient.close();
                        mqttAndroidClient.disconnect();
                        mqttAndroidClient.setCallback(null);
                        mqttAndroidClient = null;
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void TimerInitialization(){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(recording){
                    double SmokeVal =-100;
                    double GasVal =-100;
                    double TemperatureVal =-100;
                    double UVVal =-100;
                    double longtitude=default_long;
                    double latitude=default_lat;

                    for (int i = 0; i < sensor_list.size(); i++)
                    {
                        if(sensor_list.get(i).getEnabled()){
                            if(sensor_list.get(i).getType()==type.SMOKE){
                                SmokeVal=sensor_list.get(i).getValue();
                            }
                            else if(sensor_list.get(i).getType()==type.GAS){
                                GasVal=sensor_list.get(i).getValue();
                            }
                            else if(sensor_list.get(i).getType()==type.TEMPERATURE){
                                TemperatureVal=sensor_list.get(i).getValue();
                            }
                            else if(sensor_list.get(i).getType()==type.RADIATION){
                                UVVal=sensor_list.get(i).getValue();
                            }
                        }

                    }
                    if(!getIsManual()){
                        longtitude=global_long;
                        latitude=global_lat;
                    }

                    int iot_id=1;
                    if(!getIsIoT1()){
                        iot_id=2;
                    }


                    publishMessage(String.valueOf(iot_id)+","+String.valueOf(latitude)+","+String.valueOf(longtitude)+","+String.valueOf(0)+","+String.valueOf(SmokeVal)+","+String.valueOf(GasVal)+","+String.valueOf(TemperatureVal)+","+String.valueOf(UVVal)+","+String.valueOf(batteryPct));
                }
                if(!getIsManual()){
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
                        location_client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    setGlobalLong(location.getLongitude());
                                    setGlobalLat(location.getLatitude());
                                    Log.d("GPS","found");
                                }
                            }
                        });
                    }
                }
            }
        }, 0, 1000);
    }
    private void managingSensorTabLayout(){
        sensor_type_text_view=(TextView) findViewById(R.id.sensor_type_text);
        value_slider_object=(Slider)findViewById(R.id.value_slider);
        sensor_enabled=(CheckBox)findViewById(R.id.sensor_enabled_check_box);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);


        for (int i = 0; i < sensor_list.size(); i++)
        {
            TabLayout.Tab new_tab=tabLayout.newTab();
            new_tab.setText(String.valueOf(i+1));
            new_tab.setId(i);

            tabLayout.addTab(new_tab);
        }
        Sensor current=sensor_list.get(0);
        type t=sensor_list.get(0).getType();
        String s="Smoke";
        switch(t) {
            case SMOKE:
                s="Smoke";
                break;
            case GAS:
                s="Gas";
                break;
            case TEMPERATURE:
                s="Temperature";
                break;
            case RADIATION:
                s="Radiation";
                break;
            default:
                s="Smoke";
        }
        sensor_type_text_view.setText(s);
        sensor_enabled.setChecked(current.getEnabled());
        value_slider_object.setValueFrom(current.getMinValue());
        value_slider_object.setValueTo(current.getMaxValue());
        value_slider_object.setValue(current.getValue());
        slider_on_change_listener=new Slider.OnChangeListener(){
            @Override
            public void onValueChange (Slider slider,float value,boolean fromUser){
                if(fromUser){
                    sensor_list.get(currentTab).setValue(value);
                }
            }
        };
        value_slider_object.addOnChangeListener(slider_on_change_listener);
        sensor_enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                sensor_list.get(currentTab).setEnabled(isChecked);
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab=tab.getId();
                Log.d("Testing",String.valueOf( tab.getId()));
                type t=sensor_list.get(tab.getId()).getType();
                Sensor current=sensor_list.get(tab.getId());
                String s="Smoke";
                switch(t) {
                    case SMOKE:
                        s="Smoke";
                        break;
                    case GAS:
                        s="Gas";
                        break;
                    case TEMPERATURE:
                        s="Temperature";
                        break;
                    case RADIATION:
                        s="Radiation";
                        break;
                    default:
                        s="Smoke";
                }
                sensor_type_text_view.setText(s);
                sensor_enabled.setChecked(current.getEnabled());
                value_slider_object.setValueFrom(-1000000);
                value_slider_object.setValueTo(1000000);
                value_slider_object.setValue(current.getValue());
                value_slider_object.setValueFrom(current.getMinValue());
                value_slider_object.setValueTo(current.getMaxValue());



            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }





}