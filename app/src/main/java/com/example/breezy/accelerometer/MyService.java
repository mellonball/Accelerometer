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

    private boolean mStartCollectingData = false;
    private SensorManager mSensorManager;
    private Float mAccelerometerVal = 0.0f, mGyroscopeVal = 0.0f;
    private boolean mAccelerometerDataIsCollected, mGyroscopeDataIsCollected;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private IBinder myTempBinder = new MyBinder();
    private int mNumValuesNeeded = 100;
    ArrayList<Float> mAccelerometerValues = new ArrayList<>();
    ArrayList<Float> mGyroscopeValues = new ArrayList<>();

    public static final String ACCELEROMETER_DATA = "Accelerometer Data";
    public static final String GYROSCOPE_DATA = "Gyroscope Data";
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

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        mMovementAcceleration = new AccelerationItem(0,0,0);
        mGravityAcceleration = new AccelerationItem(0,0,0);

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
                mGravityAcceleration.x = event.values[0] * kFilteringFactor + mGravityAcceleration.x * (1.0f - kFilteringFactor);
                mGravityAcceleration.y = event.values[1] * kFilteringFactor + mGravityAcceleration.y * (1.0f - kFilteringFactor);
                mGravityAcceleration.z = event.values[2] * kFilteringFactor + mGravityAcceleration.z * (1.0f - kFilteringFactor);
                mMovementAcceleration.x = event.values[0] - mGravityAcceleration.x;
                mMovementAcceleration.y= event.values[1] - mGravityAcceleration.y;
                mMovementAcceleration.z = event.values[2] - mGravityAcceleration.z;
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

        /*if (mStartCollectingData)
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
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //function to start collecting accelerometer and mGyroscope values
    //probably won't need this
    public void collectSensorData() {
        mStartCollectingData = true;

    }

    //probably won't need this
    public Bundle getSensorData() {
        Bundle bundle = new Bundle();
        if ( isSensorDataReady() ) {
            mStartCollectingData = false;
            bundle.putFloat(ACCELEROMETER_DATA, mAccelerometerVal);
            bundle.putFloat(GYROSCOPE_DATA, mGyroscopeVal);
        }
        //Bundle returnBundle = new Bundle();
        //returnBundle.putFloat(ACCELEROMETER_X, m);
        return bundle;
    }

    public boolean isSensorDataReady() {
        return ( mGyroscopeDataIsCollected && mAccelerometerDataIsCollected);
    }

    //TODO: Used to pass real time accelerometer data to main view, won't need this when we're done
    public void addListener(ISensorDataListener listener) {
        mSensorDataListeners.add(listener);
    }

    public void disconnect() {
        mSensorManager.unregisterListener(this);
    }


    //We'll do our evaluation here and pass back the result
    //This will probably need tweaking, this is just a first guess at the algorithm.
    private HistoryItem.UserActivity getUserActivity() {
        HistoryItem.UserActivity ret;
        if( Math.abs(mGravityAcceleration.y) > Math.abs(mGravityAcceleration.x) &&
                Math.abs(mGravityAcceleration.y) > Math.abs(mGravityAcceleration.z) ) {
            return HistoryItem.UserActivity.SITTING;
        } else if (Math.abs(mGravityAcceleration.x) > Math.abs(mGravityAcceleration.y) ||
                Math.abs(mGravityAcceleration.z) > Math.abs(mGravityAcceleration.y) ) {
            return HistoryItem.UserActivity.SLEEPING;
        } else {
            return HistoryItem.UserActivity.WALKING;
        }
    }



}
