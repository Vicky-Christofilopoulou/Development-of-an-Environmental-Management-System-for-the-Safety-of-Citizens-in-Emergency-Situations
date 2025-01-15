package com.example.iotdevice2pahotest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import android.util.Log;
import androidx.core.view.WindowCompat;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity{
    EditText ip_text;
    EditText port_text;
    CheckBox cb;
    CheckBox cbiot;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        Log.d("Testing","onCreateSettings()");
        settingsFieldsSetup();
        mainButtonSetup();
        exitButtonSetup();
        newSensorSetup();



    }
    private void settingsFieldsSetup(){
        ip_text=(EditText)findViewById(R.id.text_input_ip);
        ip_text.setText(MainActivity.getServerIp());
        port_text=(EditText)findViewById(R.id.text_input_port);
        port_text.setText(String.valueOf(MainActivity.getServerPort()));
        cb=( CheckBox ) findViewById( R.id.manual_check_box );
        cb.setChecked(MainActivity.getIsManual());
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                MainActivity.setIsManual(cb.isChecked());

            }
        });
        ip_text.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                MainActivity.setServerIp(ip_text.getText().toString());
            }
        });
        port_text.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(port_text.length()!=0)
                    MainActivity.setServerPort(Integer.parseInt(port_text.getText().toString()));
            }
        });
        cbiot=( CheckBox ) findViewById( R.id.iot_id_check_box );
        cbiot.setChecked(MainActivity.getIsIoT1());
        cbiot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                MainActivity.setIsIoT1(cbiot.isChecked());

            }
        });
    }
    private void mainButtonSetup(){
        Button btn = (Button)findViewById(R.id.main_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingsActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void exitButtonSetup(){
        Button btn_exit = (Button)findViewById(R.id.button_exit);

        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ab = new AlertDialog.Builder(SettingsActivity.this);
                ab.setTitle("Exit!");
                ab.setMessage("Are you sure you want to exit the Application?");
                ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        finishAffinity();

                        System.exit(0);
                    }
                });
                ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                ab.show();

            }
        });
    }
    private void newSensorSetup(){
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.add_sensor_button);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent=new Intent(SettingsActivity.this, NewSensor.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

}
