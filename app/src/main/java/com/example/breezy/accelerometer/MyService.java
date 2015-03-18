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
import java.util.List;


public class MyService extends Service implements SensorEventListener{


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private IBinder myTempBinder = new MyBinder();

    public static final String ACCELEROMETER_X = "Accelerometer X Value";
    public static final String ACCELEROMETER_Y = "Accelerometer Y Value";
    public static final String ACCELEROMETER_Z = "Accelerometer Z Value";
    public static final String GRAVITY_X = "Gravity X Value";
    public static final String GRAVITY_Y = "Gravity Y Value";
    public static final String GRAVITY_Z = "Gravity Z Value";
    public static final String ACTIVITY_STATUS = "Activity Status";

    private final String TAG = MyService.class.getCanonicalName();

    private List<ISensorDataListener> mSensorDataListeners;
    private AccelerationItem mMovementAcceleration;
    private AccelerationItem mGravityAcceleration;
    private final float kFilteringFactor = 0.1f;

    //Locking acceleration and gravity during collection
    private final Object lock = new Object();

    //TODO: Temp Interface for passing real time accelerometer data to MainActivity, remove before finish.
    public interface ISensorDataListener {
        public void onSensorDataChanged(Bundle data);
    }

    public class AccelerationItem {
        public float x,y,z;
        public AccelerationItem(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMovementAcceleration = new AccelerationItem(0,0,0);
        mGravityAcceleration = new AccelerationItem(0,0,0);
        //Temporary variable for displaying data in MainActivity
        mSensorDataListeners = new ArrayList<>();
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

        /*
         * We are using filter techniques that are listed in the Android documentation.
         */
        for(ISensorDataListener listener : mSensorDataListeners ) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
                //Sync so that this event can be completed before polling for data (I think?)
                synchronized (lock) {
                    mGravityAcceleration.x = event.values[0] * kFilteringFactor + mGravityAcceleration.x * (1.0f - kFilteringFactor);
                    mGravityAcceleration.y = event.values[1] * kFilteringFactor + mGravityAcceleration.y * (1.0f - kFilteringFactor);
                    mGravityAcceleration.z = event.values[2] * kFilteringFactor + mGravityAcceleration.z * (1.0f - kFilteringFactor);
                    mMovementAcceleration.x = event.values[0] - mGravityAcceleration.x;
                    mMovementAcceleration.y= event.values[1] - mGravityAcceleration.y;
                    mMovementAcceleration.z = event.values[2] - mGravityAcceleration.z;
                }
                Bundle sensorBundle = new Bundle();
                sensorBundle.putFloat(ACCELEROMETER_X, mMovementAcceleration.x);
                sensorBundle.putFloat(ACCELEROMETER_Y, mMovementAcceleration.y);
                sensorBundle.putFloat(ACCELEROMETER_Z, mMovementAcceleration.z);
                sensorBundle.putFloat(GRAVITY_X, mGravityAcceleration.x);
                sensorBundle.putFloat(GRAVITY_Y, mGravityAcceleration.y);
                sensorBundle.putFloat(GRAVITY_Z, mGravityAcceleration.z);
                sensorBundle.putString(ACTIVITY_STATUS, getUserActivity().toString() );
                listener.onSensorDataChanged(sensorBundle);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //TODO: Used to pass real time accelerometer data to main view, won't need this when we're done
    public void addListener(ISensorDataListener listener) {
        mSensorDataListeners.add(listener);
    }
    public void removeListener(ISensorDataListener listener) { mSensorDataListeners.remove(listener); }

    //Used to disconnect sensors
    public void unregisterSensors() {
        mSensorManager.unregisterListener(this);
    }

    //Used to connect to our sensors when we start collecting data
    public void registerSensors() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void clearCurrentData() {
        synchronized (lock) {
            mMovementAcceleration = new AccelerationItem(0, 0, 0);
            mGravityAcceleration = new AccelerationItem(0, 0, 0);
        }
    }


    //We'll do our evaluation here and pass back the result
    //This will probably need tweaking, this is just a first guess at the algorithm.
    private HistoryItem.UserActivity getUserActivity() {
        HistoryItem.UserActivity ret;
        synchronized (lock) {
            if (Math.abs(mGravityAcceleration.y) > Math.abs(mGravityAcceleration.x) &&
                    Math.abs(mGravityAcceleration.y) > Math.abs(mGravityAcceleration.z)) {
                return HistoryItem.UserActivity.SITTING;
            } else if (Math.abs(mGravityAcceleration.x) > Math.abs(mGravityAcceleration.y) ||
                    Math.abs(mGravityAcceleration.z) > Math.abs(mGravityAcceleration.y)) {
                return HistoryItem.UserActivity.SLEEPING;
            } else {
                return HistoryItem.UserActivity.WALKING;
            }
        }
    }



}
