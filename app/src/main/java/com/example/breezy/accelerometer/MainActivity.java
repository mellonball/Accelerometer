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


public class MainActivity extends ActionBarActivity implements View.OnClickListener  {

    private Button mCreateButton, mInvokeButton, mDataButton;
    private TextView mRsaTextView, mRsgTextView;
    private MyService mServiceobj;
    private boolean mBound;
    private int mCurrentScreenOrientation;

    private final String TAG = MainActivity.class.getCanonicalName();
    private final String RSA_KEY = "RSA";
    private final String RSG_KEY = "RSG";

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

        mRsaTextView = (TextView) findViewById(R.id.rsa);
        mRsgTextView = (TextView) findViewById(R.id.rsg);

        if (savedInstanceState != null) {
            mRsaTextView.setText(savedInstanceState.getString(RSA_KEY));
            mRsgTextView.setText(savedInstanceState.getString(RSG_KEY));
        }

        mBound = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        Log.d(TAG, "RSA Text: " + mRsaTextView.getText().toString());
        Log.d(TAG, "RSG Text: " + mRsgTextView.getText().toString());
        outState.putString(RSA_KEY, mRsaTextView.getText().toString());
        outState.putString(RSG_KEY, mRsgTextView.getText().toString());
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonCreateBS:
                Intent boundIntent = new Intent(this, MyService.class);
                bindService(boundIntent, mConnection, Context.BIND_AUTO_CREATE);
                mBound = true;
                Log.d(TAG, "You clicked create bs");
                break;

            case R.id.buttonInvoke:
                if (mBound) {
                    mServiceobj.collectSensorData();
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
                        mRsaTextView.setText(rsaVal.toString());
                        mRsgTextView.setText(rsgVal.toString());
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
            Toast.makeText(MainActivity.this, "bound service started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "The service has been disconnected", Toast.LENGTH_SHORT).show();

        }
    };
}
