package com.example.breezy.accelerometer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
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
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }*/

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
                System.out.println("You clicked create bs");
                break;

            case R.id.buttonInvoke:
                serviceobj.collectSensorData();
                Toast.makeText(MainActivity.this, "collecting", Toast.LENGTH_SHORT).show();
                break;

            case R.id.buttonData:
                Bundle a = serviceobj.getSensorData();
                if (serviceobj.finalAccelerometerVal && serviceobj.finalGyroscopeVal) {
                    Float rsaVal = a.getFloat("accelerometerVal");
                    Float rsgVal = a.getFloat("gyroscopeVal");
                    rsa.setText(rsaVal.toString());
                    rsg.setText(rsgVal.toString());
                } else {
                    Toast.makeText(MainActivity.this, "a is null still", Toast.LENGTH_SHORT).show();
                }
                unbindService(mConnection);
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
