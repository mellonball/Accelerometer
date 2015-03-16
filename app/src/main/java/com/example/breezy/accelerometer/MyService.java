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
import android.util.Log;

import java.util.ArrayList;


public class MyService extends Service implements SensorEventListener{

    private boolean mStartCollectingData = false;
    private SensorManager mSensorManager;
    private Float mAccelerometerVal = 0.0f, mGyroscopeVal = 0.0f;
    private boolean mAccelerometerDataIsCollected, mGyroscopeDataIsCollected;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private IBinder myTempBinder = new MyBinder();
    private Bundle mReturnData = new Bundle();
    private int mNumValuesNeeded = 100;
    ArrayList<Float> mAccelerometerValues = new ArrayList<>();
    ArrayList<Float> mGyroscopeValues = new ArrayList<>();

    public static final String ACCELEROMETER_DATA = "Accelerometer Data";
    public static final String GYROSCOPE_DATA = "Gyroscope Data";

    private final String TAG = MyService.class.getCanonicalName();

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
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
        if (mStartCollectingData)
        {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && mAccelerometerValues.size() < mNumValuesNeeded) {
                mAccelerometerValues.add(event.values[1]);
                Log.d(TAG, "We are in accel " + event.values[1] + " size: " + mAccelerometerValues.size());
            }

            else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && mGyroscopeValues.size() < mNumValuesNeeded){
                mGyroscopeValues.add(event.values[0]);
                Log.d(TAG, "We are in gyroscope " + event.values[1] + " size: " + mGyroscopeValues.size());
            }

            if (mAccelerometerValues.size() == mNumValuesNeeded && mAccelerometerDataIsCollected == false){
                mAccelerometerDataIsCollected = true;

                for (int i=0; i< mNumValuesNeeded; i++){
                    mAccelerometerVal += mAccelerometerValues.get(i);
                    //summing
                }
                mAccelerometerVal /= 10.0f;
                Log.d(TAG, "FINAL ACCELVAL = " + mAccelerometerVal);

                // for testing print to screen when done
                // MainActivity.rsa.setText(mAccelerometerVal.toString());
            }

            if (mGyroscopeValues.size() == mNumValuesNeeded && mGyroscopeDataIsCollected == false){
                mGyroscopeDataIsCollected = true;

                for (int i=0; i< mNumValuesNeeded; i++){
                    mGyroscopeVal += mGyroscopeValues.get(i);
                }
                mGyroscopeVal /= 10.0f;
               Log.d(TAG, "FINAL GYROSC VAL = " + mGyroscopeVal);

                // for testing, print to screen when done.
                // MainActivity.rsg.setText(mGyroscopeVal.toString());

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //function to start collecting accelerometer and mGyroscope values
    public void collectSensorData() {
        mStartCollectingData = true;

    }

    public Bundle getSensorData() {
        if ( isSensorDataReady() ) {
            mStartCollectingData = false;
            mReturnData.putFloat(ACCELEROMETER_DATA, mAccelerometerVal);
            mReturnData.putFloat(GYROSCOPE_DATA, mGyroscopeVal);
        }
        return mReturnData;
    }

    public boolean isSensorDataReady() {
        return ( mGyroscopeDataIsCollected && mAccelerometerDataIsCollected);
    }



}
