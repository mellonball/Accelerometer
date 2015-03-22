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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class MyService extends Service implements SensorEventListener {

    private static int SIT = 0;
    private static int SLEEP = 1;
    private static int WALK  = 2;
    private static int state;
    private static int prevState;

    public static final String ACCELEROMETER_X = "Accelerometer X Value";
    public static final String ACCELEROMETER_Y = "Accelerometer Y Value";
    public static final String ACCELEROMETER_Z = "Accelerometer Z Value";
    public static final String GRAVITY_X = "Gravity X Value";
    public static final String GRAVITY_Y = "Gravity Y Value";
    public static final String GRAVITY_Z = "Gravity Z Value";
    public static final String ACTIVITY_STATUS = "Activity Status";

    private final String TAG = MyService.class.getCanonicalName();
    private final float kFilteringFactor = 0.1f;
    private final int POLLING_INTERVAL_SECONDS = 1;
    //Locking acceleration and gravity during collection
    //Work done in same thread, so can be sync'd
    private final Object mSensorDataLock = new Object();
    //Locking polling, only permit one lock
    //Need semaphore for multi threads
    private final static Semaphore mPollingDataLock = new Semaphore(1);

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private IBinder myTempBinder = new MyBinder();
    private List<ISensorDataListener> mSensorDataListeners;
    private Map<HistoryItem.UserActivity, Integer> mSampledUserActivity;
    private AccelerationItem mMovementAcceleration;
    private AccelerationItem mGravityAcceleration;
    private ScheduledExecutorService mThreadScheduler;
    private ScheduledFuture mActivityPoller;
    private Runnable mPollUserActivity;

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
        mSampledUserActivity = new HashMap<>();
        initializeSampledActivity();
        mThreadScheduler = Executors.newSingleThreadScheduledExecutor();
        mPollUserActivity = new Runnable() {

            @Override
            public void run() {
                try {
                    if( mPollingDataLock.tryAcquire() ) {
                        HistoryItem.UserActivity currentActivity = getUserActivity();
                        Integer activityCount = mSampledUserActivity.get(currentActivity) + 1;
                        mSampledUserActivity.put(currentActivity, activityCount);
                        Log.d(TAG, "Collected sample: " + currentActivity);

                        mPollingDataLock.release();
                    } else {
                        Log.d(TAG, "Couldn't collect sample, locked out");
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Polling activity Exception: " + e.toString());
                }
            }
        };
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
                synchronized (mSensorDataLock) {
                    mGravityAcceleration.x = event.values[0] * kFilteringFactor + mGravityAcceleration.x * (1.0f - kFilteringFactor);
                    mGravityAcceleration.y = event.values[1] * kFilteringFactor + mGravityAcceleration.y * (1.0f - kFilteringFactor);
                    mGravityAcceleration.z = event.values[2] * kFilteringFactor + mGravityAcceleration.z * (1.0f - kFilteringFactor);
                    mMovementAcceleration.x = event.values[0] - mGravityAcceleration.x;
                    mMovementAcceleration.y= event.values[1] - mGravityAcceleration.y;
                    mMovementAcceleration.z = event.values[2] - mGravityAcceleration.z;
                }
                if( MainActivity.DEBUG ) {
                    Bundle sensorBundle = new Bundle();
                    //Log.d(TAG, "New readings");

                    sensorBundle.putFloat(ACCELEROMETER_X, mMovementAcceleration.x);
                    //Log.d(TAG, "mMovementAcceleration.x = " + mMovementAcceleration.x);

                    sensorBundle.putFloat(ACCELEROMETER_Y, mMovementAcceleration.y);
                    //Log.d(TAG, "mMovementAcceleration.y = " + mMovementAcceleration.y);

                    sensorBundle.putFloat(ACCELEROMETER_Z, mMovementAcceleration.z);
                    //Log.d(TAG, "mMovementAcceleration.z = " + mMovementAcceleration.z);

                    sensorBundle.putFloat(GRAVITY_X, mGravityAcceleration.x);
                    //Log.d(TAG, "mGravityAcceleration.x = " + mGravityAcceleration.x);

                    sensorBundle.putFloat(GRAVITY_Y, mGravityAcceleration.y);
                    //Log.d(TAG, "mGravityAcceleration.y = " + mGravityAcceleration.y);

                    sensorBundle.putFloat(GRAVITY_Z, mGravityAcceleration.z);
                    //Log.d(TAG, "mGravityAcceleration.z = " + mGravityAcceleration.z);

                    sensorBundle.putString(ACTIVITY_STATUS, getUserActivity().toString() );
                    listener.onSensorDataChanged(sensorBundle);
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //Used for debug view
    public void addListener(ISensorDataListener listener) {
        mSensorDataListeners.add(listener);
    }
    public void removeListener(ISensorDataListener listener) { mSensorDataListeners.remove(listener); }

    //Used to disconnect sensors
    public void stopDataCollection() {
        if( mActivityPoller != null ) {
            mActivityPoller.cancel(true);
        }
        mSensorManager.unregisterListener(this);
    }

    //Used to connect to our sensors when we start collecting data
    public void startDataCollection() {
        mActivityPoller = mThreadScheduler.scheduleAtFixedRate(mPollUserActivity,
                0,
                POLLING_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void clearCurrentData() {
        synchronized (mSensorDataLock) {
            mMovementAcceleration = new AccelerationItem(0, 0, 0);
            mGravityAcceleration = new AccelerationItem(0, 0, 0);
        }
    }

    public HistoryItem.UserActivity getSampledUserActivity()  {
        while( !mPollingDataLock.tryAcquire() ) {
            //Do nothing until lock is acquired
        }
        //Need to find activity with most samples
        HistoryItem.UserActivity maxSampledActivity = HistoryItem.UserActivity.SLEEPING;
        Integer maxSamples = mSampledUserActivity.get(HistoryItem.UserActivity.SLEEPING);
        for(Map.Entry<HistoryItem.UserActivity, Integer> sample : mSampledUserActivity.entrySet()) {
            Log.d(TAG, "Activity: " + sample.getKey().toString() + " Samples: " + sample.getValue().toString());
            if(sample.getValue() > maxSamples) {
                maxSamples = sample.getValue();
                maxSampledActivity = sample.getKey();
            }
        }
        Log.d(TAG, "Winner: " + maxSampledActivity.toString());
        initializeSampledActivity();
        mPollingDataLock.release();
        return maxSampledActivity;
    }


    //We'll do our evaluation here and pass back the result
    //This will probably need tweaking, this is just a first guess at the algorithm.
    private HistoryItem.UserActivity getUserActivity() {
        HistoryItem.UserActivity ret;
        synchronized (mSensorDataLock) {
            if (Math.abs(mGravityAcceleration.x) > 3 + Math.abs(mGravityAcceleration.y) ||
                    Math.abs(mGravityAcceleration.z) > 3 + Math.abs(mGravityAcceleration.y)) {
            //if (Math.abs(mGravityAcceleration.z) > Math.abs(mGravityAcceleration.y) && ( Math.abs(mGravityAcceleration.z) > Math.abs(mGravityAcceleration.x) || Math.abs(mGravityAcceleration.z) > 7 )) {
                state = SLEEP;
                return HistoryItem.UserActivity.SLEEPING;
            }
            else if ( Math.abs(mMovementAcceleration.y) > 3.5 || Math.abs(mMovementAcceleration.z) > 3.5) { // want a state transition if the
                if (state == WALK){
                    prevState = state;
                    state = SIT;
                    return HistoryItem.UserActivity.SITTING;
                }
                else {
                    prevState = state;
                    state = WALK;
                    return HistoryItem.UserActivity.WALKING;
                }

            }
            else if (Math.abs(mMovementAcceleration.x) < 0.35 && Math.abs(mMovementAcceleration.y) < 0.35 && Math.abs(mMovementAcceleration.z) < 0.35) {
                state = SIT;
                return HistoryItem.UserActivity.SITTING;
            }
            else if ((Math.abs(mMovementAcceleration.x) > 0.35 && Math.abs(mMovementAcceleration.y) > 0.35 && Math.abs(mGravityAcceleration.z) > 1  ||
                    Math.abs(mMovementAcceleration.x) > 0.35 && Math.abs(mMovementAcceleration.z) > 0.35 && Math.abs(mGravityAcceleration.z) > 1 ||
                    Math.abs(mMovementAcceleration.z) > 0.35 && Math.abs(mMovementAcceleration.y) > 0.35 && Math.abs(mGravityAcceleration.z) > 1 )) {
                state = WALK;
                return HistoryItem.UserActivity.WALKING;
            }
            else {
                return HistoryItem.UserActivity.SITTING;
            }
        }
    }

    private void initializeSampledActivity() {
        mSampledUserActivity.put(HistoryItem.UserActivity.SITTING, 0);
        mSampledUserActivity.put(HistoryItem.UserActivity.SLEEPING, 0);
        mSampledUserActivity.put(HistoryItem.UserActivity.WALKING, 0);
    }



}
