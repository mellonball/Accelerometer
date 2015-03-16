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

    private Button create, invoke, data;
    public static TextView rsa, rsg;
    private MyService serviceobj;
    private boolean mBound;
    private int mCurrentScreenOrientation;

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final String RSA_KEY = "RSA";
    private static final String RSG_KEY = "RSG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        create = (Button) findViewById(R.id.buttonCreateBS);
        invoke = (Button) findViewById(R.id.buttonInvoke);
        data = (Button) findViewById(R.id.buttonData);

        create.setOnClickListener(this);
        invoke.setOnClickListener(this);
        data.setOnClickListener(this);

        rsa = (TextView) findViewById(R.id.rsa);
        rsg = (TextView) findViewById(R.id.rsg);

        if (savedInstanceState != null) {
            rsa.setText(savedInstanceState.getString(RSA_KEY));
            rsg.setText(savedInstanceState.getString(RSG_KEY));
        }

        mBound = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        Log.d(TAG, "RSA Text: " + rsa.getText().toString());
        Log.d(TAG, "RSG Text: " + rsg.getText().toString());
        outState.putString(RSA_KEY, rsa.getText().toString());
        outState.putString(RSG_KEY, rsg.getText().toString());
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
        // as you specify a parent activity in AndroidManifest.xml.
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
                System.out.println("You clicked create bs");
                break;

            case R.id.buttonInvoke:
                if (mBound) {
                    serviceobj.collectSensorData();
                    mCurrentScreenOrientation = getRequestedOrientation();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    Toast.makeText(MainActivity.this, "collecting", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.buttonData:
                if (mBound) {
                    Bundle a;
                    if( serviceobj != null) {
                        a = serviceobj.getSensorData();
                    } else {
                        Toast.makeText(MainActivity.this, "serviceobj is null", Toast.LENGTH_LONG).show();
                        break;
                    }
                    if (serviceobj.finalAccelerometerVal && serviceobj.finalGyroscopeVal) {
                        Float rsaVal = a.getFloat("accelerometerVal");
                        Float rsgVal = a.getFloat("gyroscopeVal");
                        rsa.setText(rsaVal.toString());
                        rsg.setText(rsgVal.toString());
                        setRequestedOrientation(mCurrentScreenOrientation);
                        mBound = false;
                        unbindService(mConnection);
                    } else {
                        Toast.makeText(MainActivity.this, "a is null still", Toast.LENGTH_SHORT).show();
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
            serviceobj = binder.getService();
            if (serviceobj == null) {
                System.out.println("Service obj is indeed null");
            }
            Toast.makeText(MainActivity.this, "bound service started", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(MainActivity.this, "The service has been disconnected", Toast.LENGTH_SHORT).show();

        }
    };
}
