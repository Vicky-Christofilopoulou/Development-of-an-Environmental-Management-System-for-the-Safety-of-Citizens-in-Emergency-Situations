package com.example.iotdevice2pahotest;
enum type {
    SMOKE,
    GAS,
    TEMPERATURE,
    RADIATION
}
public class Sensor {
    private boolean enabled;
    private type sensor_type;
    private float value;
    private float min_value;
    private float max_value;

    public Sensor(type t,float min_value_new,float max_value_new){
        sensor_type=t;
        min_value=min_value_new;
        max_value=max_value_new;
        enabled=true;
        value=min_value_new;
    }
    public void setValue(float value_new){
        value=value_new;
    }
    public float getValue(){
        return value;
    }
    public type getType(){
        return sensor_type;
    }
    public float getMinValue(){
        return min_value;
    }
    public float getMaxValue(){
        return max_value;
    }

    public boolean getEnabled(){return enabled;}
    public void setEnabled(boolean enabled_new){
        enabled=enabled_new;
    }

}
