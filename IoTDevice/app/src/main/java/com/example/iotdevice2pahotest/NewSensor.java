package com.example.iotdevice2pahotest;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import android.util.Log;
import androidx.core.view.WindowCompat;



import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

public class NewSensor extends AppCompatActivity{

    EditText min_text;
    EditText max_text;
    private Spinner dropdown;
    CheckBox cb;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_sensor);
        Log.d("Testing","onNewSensor()");

        spinnerSetup();
        cancelButtonSetup();
        createButtonSetup();




    }
    private void spinnerSetup(){
        dropdown= findViewById(R.id.spinner1);
        String[] items = new String[]{"Smoke","Gas","Temperature","Radiation"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }
    private void createButtonSetup(){
        Button create_button = (Button)findViewById(R.id.create_button);
        create_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type t;
                String value =dropdown.getSelectedItem().toString();
                switch(value) {
                    case "Smoke":
                        t=type.SMOKE;
                        break;
                    case "Gas":
                        t=type.GAS;
                        break;
                    case "Temperature":
                        t=type.TEMPERATURE;
                        break;
                    case "Radiation":
                        t=type.RADIATION;
                        break;
                    default:
                        t=type.SMOKE;
                }
                Sensor s=new Sensor(t,Float.parseFloat(min_text.getText().toString()),Float.parseFloat(max_text.getText().toString()));
                MainActivity.addSensor(s);
                Intent intent=new Intent(NewSensor.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    private void cancelButtonSetup(){
        min_text=(EditText)findViewById(R.id.min_edit_text);
        max_text=(EditText)findViewById(R.id.max_edit_text);
        Button btn = (Button)findViewById(R.id.cancel_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(NewSensor.this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

}

