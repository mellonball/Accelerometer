package com.example.breezy.accelerometer;

import android.app.Activity;
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

    private Button mCreateButton, mInvokeButton, mDataButton;
    private TextView xGravTextView, yGravTextView, zGravTextView;
    private TextView xAccelTextView, yAccelTextView, zAccelTextView, mStatusView;
    private MyService mServiceobj;
    private boolean mBound;
    private int mCurrentScreenOrientation;
    private Activity mActivity;

    private final String TAG = MainActivity.class.getCanonicalName();
    private final String RSA_KEY = "RSA";
    private final String RSG_KEY = "RSG";

    //TODO: Add call backs in this activity whenever we edit the fragment UI
    public interface IHistoryChanged {
        public void newHistoryActivity(HistoryItem item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCreateButton = (Button) findViewById(R.id.buttonCreateBS);
        mInvokeButton = (Button) findViewById(R.id.buttonInvoke);
        mDataButton = (Button) findViewById(R.id.buttonData);

        mCreateButton.setOnClickListener(this);
        mInvokeButton.setOnClickListener(this);
        mDataButton.setOnClickListener(this);


        //TODO: These were used to view real time data. Should be removed when app is done
        xAccelTextView = (TextView) findViewById(R.id.xAccelTextView);
        yAccelTextView = (TextView) findViewById(R.id.yAccelTextView);
        zAccelTextView = (TextView) findViewById(R.id.zAccelTextView);
        xGravTextView = (TextView) findViewById(R.id.xGravTextView);
        yGravTextView = (TextView) findViewById(R.id.yGravTextView);
        zGravTextView = (TextView) findViewById(R.id.zGravTextView);
        mStatusView = (TextView) findViewById(R.id.activityStatusTextView);

        //TODO: Needs to be removed unless we are saving some other state
        /*if (savedInstanceState != null) {
            mRsaTextView.setText(savedInstanceState.getString(RSA_KEY));
            mRsgTextView.setText(savedInstanceState.getString(RSG_KEY));
        }*/

        mBound = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO: Needs to be removed unless we are saving something else
        /*Log.d(TAG, "onSaveInstanceState");
        Log.d(TAG, "RSA Text: " + mRsaTextView.getText().toString());
        Log.d(TAG, "RSG Text: " + mRsgTextView.getText().toString());
        outState.putString(RSA_KEY, mRsaTextView.getText().toString());
        outState.putString(RSG_KEY, mRsgTextView.getText().toString());*/
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
        /*if(!mBound) {
            Intent boundIntent = new Intent(this, MyService.class);
            bindService(boundIntent, mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause'd");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume'd");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop'd");
         //Unbind from the service
        if (mBound) {
            mServiceobj.disconnect();
            unbindService(mConnection);
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
            case R.id.buttonCreateBS:
                Intent boundIntent = new Intent(this, MyService.class);
                bindService(boundIntent, mConnection, Context.BIND_AUTO_CREATE);
                Log.d(TAG, "You clicked create bs");
                break;

            case R.id.buttonInvoke:
                if (mBound) {
                    mServiceobj.collectSensorData();
                    mServiceobj.addListener(this);
                    mCurrentScreenOrientation = getRequestedOrientation();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    Toast.makeText(MainActivity.this, "collecting", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonData:
                if (mBound) {
                    Bundle returnData;
                    if( mServiceobj != null) {
                        returnData = mServiceobj.getSensorData();
                    } else {
                        Toast.makeText(MainActivity.this, "serviceobj is null", Toast.LENGTH_LONG).show();
                        break;
                    }
                    if ( mServiceobj.isSensorDataReady() ) {
                        Float rsaVal = returnData.getFloat(MyService.ACCELEROMETER_DATA);
                        Float rsgVal = returnData.getFloat(MyService.GYROSCOPE_DATA);
                        //mRsaTextView.setText(rsaVal.toString());
                        //mRsgTextView.setText(rsgVal.toString());
                        setRequestedOrientation(mCurrentScreenOrientation);
                        mBound = false;
                        unbindService(mConnection);
                    } else {
                        Toast.makeText(MainActivity.this, "Return data is null still", Toast.LENGTH_SHORT).show();
                        break;
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Service is not registered", Toast.LENGTH_LONG).show();
                }
                break;
        }
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
