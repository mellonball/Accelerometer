package com.example.breezy.accelerometer;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import java.util.ArrayList;


public class MyService extends Service implements SensorEventListener{

    private boolean startCollecting = false;
    private SensorManager mysensormanager;
    private Float accelerometerVal = 0.0f, gyroscopeVal = 0.0f;
    public boolean finalAccelerometerVal, finalGyroscopeVal;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private IBinder myTempBinder = new MyBinder();
    public Bundle a = new Bundle();
    private int numVals = 100;
    ArrayList<Float> accelValues = new ArrayList<>();
    ArrayList<Float> gyroscopeValues = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mysensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mysensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = mysensormanager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mysensormanager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mysensormanager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public MyService() {
    }

    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return myTempBinder;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (startCollecting)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && accelValues.size() < numVals) {
                accelValues.add(event.values[1]);
                System.out.println("We are in accel " + event.values[1] + " size: " + accelValues.size());
            }

            else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && gyroscopeValues.size() < numVals){
                gyroscopeValues.add(event.values[0]);
                System.out.println("We are in gyroscope " + event.values[1] + " size: " + gyroscopeValues.size());
            }

            if (accelValues.size() == numVals && finalAccelerometerVal == false){
                finalAccelerometerVal = true;
                if (finalGyroscopeVal == true) {
                    startCollecting = false;
                }

                //mysensormanager.unregisterListener(this);
                for (int i=0; i<numVals; i++){
                    accelerometerVal += accelValues.get(i);
                    //summing
                    // System.out.println(accelValues.get(i));
                }
                accelerometerVal /= 10.0f;
                System.out.println("FINAL ACCELVAL = " + accelerometerVal);

                // for testing print to screen when done
                // MainActivity.rsa.setText(accelerometerVal.toString());
            }

            if (gyroscopeValues.size() == numVals && finalGyroscopeVal == false){
                finalGyroscopeVal = true;
                if (finalAccelerometerVal == true){
                    startCollecting = false;
                }
                //mysensormanager.unregisterListener(this);
                for (int i=0; i<numVals; i++){
                    gyroscopeVal += gyroscopeValues.get(i);
                    //System.out.println(gyroscopeValues.get(i));
                }
                gyroscopeVal /= 10.0f;
                System.out.println("FINAL GYROSC VAL = " + gyroscopeVal);

                // for testing, print to screen when done.
                // MainActivity.rsg.setText(gyroscopeVal.toString());

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //function to start collecting accelerometer and gyroscope values
    public void collectSensorData() {

        startCollecting = true;
    }

    public Bundle getSensorData() {
        if (finalGyroscopeVal && finalAccelerometerVal) {
            a.putFloat("accelerometerVal", accelerometerVal);
            a.putFloat("gyroscopeVal", gyroscopeVal);
        }
        return a;
    }



}
