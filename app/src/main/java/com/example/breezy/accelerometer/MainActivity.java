package com.example.breezy.accelerometer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, MyService.ISensorDataListener  {

    private Button mStartActivityDetection, mStopActivityDetection;
    private TextView xGravTextView, yGravTextView, zGravTextView;
    private TextView xAccelTextView, yAccelTextView, zAccelTextView, mStatusView;
    private MyService mServiceobj;
    private boolean mBound, mDetecting;
    private int mCurrentScreenOrientation;

    private final String TAG = MainActivity.class.getCanonicalName();
    private final String USER_SCREEN_ORIENTATION = "Orientation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStartActivityDetection = (Button) findViewById(R.id.startDataCollection);
        mStopActivityDetection = (Button) findViewById(R.id.stopDataCollection);

        mStartActivityDetection.setOnClickListener(this);
        mStopActivityDetection.setOnClickListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        //TODO: These were used to view real time data. Should be removed when app is done
        xAccelTextView = (TextView) findViewById(R.id.xAccelTextView);
        yAccelTextView = (TextView) findViewById(R.id.yAccelTextView);
        zAccelTextView = (TextView) findViewById(R.id.zAccelTextView);
        xGravTextView = (TextView) findViewById(R.id.xGravTextView);
        yGravTextView = (TextView) findViewById(R.id.yGravTextView);
        zGravTextView = (TextView) findViewById(R.id.zGravTextView);
        mStatusView = (TextView) findViewById(R.id.activityStatusTextView);


        if (savedInstanceState != null) {
           //mCurrentScreenOrientation = savedInstanceState.getInt(USER_SCREEN_ORIENTATION);
           //setRequestedOrientation(mCurrentScreenOrientation);
        }

        mBound = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //outState.putInt(USER_SCREEN_ORIENTATION, mCurrentScreenOrientation);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        /* The bundle in onSaveInstanceState is also passed to onCreate, so we
         * are updating the UI there instead of here.
         */
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart'd");
        if(!mBound) {
            bindSensorService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause'd");
        /*if(mDetecting) {
            Toast.makeText(MainActivity.this, "Activity Detection Halted", Toast.LENGTH_SHORT).show();
            mDetecting = false;
        }*/

        if(mBound) {
            mServiceobj.unregisterSensors();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume'd");


        if(mBound) {
            mServiceobj.registerSensors();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop'd");
         //Unbind from the service
        if (mBound) {
            unbindSensorService();
            mBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify mReturnData parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: Needs to be cleaned up, there's a lot here we don't need
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.startDataCollection:
                if(mBound && !mDetecting) {
                    startActivityDetection();
                    mDetecting = true;
                    Log.d(TAG, "Sensors registered, collecting data");
                    Toast.makeText(MainActivity.this, "Detecting activity ... ", Toast.LENGTH_SHORT).show();
                } else if( !mBound ) {
                    Toast.makeText(MainActivity.this, "Service not bound", Toast.LENGTH_SHORT).show();
                    bindSensorService();
                } else if( mDetecting ) {
                    Toast.makeText(MainActivity.this, "Detection in progress ... ", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.stopDataCollection:
                if (mBound && mDetecting) {
                    stopActivityDetection();
                    mDetecting = false;
                    mServiceobj.clearCurrentData();
                    Toast.makeText(MainActivity.this, "Detection stopped", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Not Collecting Data", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void bindSensorService() {
        Intent boundIntent = new Intent(this, MyService.class);
        bindService(boundIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindSensorService() {
        mServiceobj.unregisterSensors();
        mServiceobj.removeListener(this);
        unbindService(mConnection);
    }

    private void startActivityDetection() {
        mServiceobj.addListener(this);
        mServiceobj.registerSensors();
    }

    private void stopActivityDetection() {
        mServiceobj.removeListener(this);
        mServiceobj.unregisterSensors();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.MyBinder binder = (MyService.MyBinder) service;
            mServiceobj = binder.getService();
            if (mServiceobj == null) {
                Log.d(TAG, "Service obj is indeed null");
            }
            mBound = true;
            Toast.makeText(MainActivity.this, "bound service started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "The service has been disconnected", Toast.LENGTH_SHORT).show();

        }
    };

    //TODO: Used to view real time data, need to remove it when we're done
    @Override
    public void onSensorDataChanged(Bundle data) {

        xAccelTextView.setText(getString(R.string.x_move_acceleration) + ": " +
                String.format("%.4f", data.getFloat(MyService.ACCELEROMETER_X)));
        yAccelTextView.setText(getString(R.string.y_move_acceleration) + ": " +
                String.format("%.4f",data.getFloat(MyService.ACCELEROMETER_Y)));
        zAccelTextView.setText(getString(R.string.z_move_acceleration) + ": " +
                String.format("%.4f",data.getFloat(MyService.ACCELEROMETER_Z)));
        xGravTextView.setText(getString(R.string.x_gravity_acceleration) + ": " +
                String.format("%.4f",data.getFloat(MyService.GRAVITY_X)));
        yGravTextView.setText(getString(R.string.y_gravity_acceleration) + ": " +
                String.format("%.4f",data.getFloat(MyService.GRAVITY_Y)));
        zGravTextView.setText(getString(R.string.z_gravity_acceleration) + ": " +
                String.format("%.4f",data.getFloat(MyService.GRAVITY_Z)));
        mStatusView.setText(data.getString(MyService.ACTIVITY_STATUS));
    }
}
