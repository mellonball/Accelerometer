package com.example.breezy.accelerometer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.example.breezy.accelerometer.HistoryItem.UserActivity;


public class MainActivity extends ActionBarActivity implements View.OnClickListener, MyService.ISensorDataListener {

    private Button mStartActivityDetection, mStopActivityDetection;
    private TextView xGravTextView, yGravTextView, zGravTextView;
    private TextView xAccelTextView, yAccelTextView, zAccelTextView, mStatusView;
    private MyService mServiceobj;
    private ActivityHistoryFragment mHistoryFragment;
    private ScheduledExecutorService mThreadScheduler;
    private ScheduledFuture mActivityPoller;
    private Handler mHandler;
    private Runnable mPollUserActivity;
    private Date mPollStartTime;

    private boolean mBound, mDetecting;

    // set to true to see real-time accelerometer readings displayed on the UI
    public static final boolean DEBUG = false;

    private final String TAG = MainActivity.class.getCanonicalName();
    private final int POLLING_INTERVAL_MINUTES = 2;

    private File writeDir;
    private File writeFile;
    FileWriter fw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(DEBUG) {
            setContentView(R.layout.activity_main_debug);
            xAccelTextView = (TextView) findViewById(R.id.xAccelTextView);
            yAccelTextView = (TextView) findViewById(R.id.yAccelTextView);
            zAccelTextView = (TextView) findViewById(R.id.zAccelTextView);
            xGravTextView = (TextView) findViewById(R.id.xGravTextView);
            yGravTextView = (TextView) findViewById(R.id.yGravTextView);
            zGravTextView = (TextView) findViewById(R.id.zGravTextView);
            mStatusView = (TextView) findViewById(R.id.activityStatusTextView);
        } else {
            setContentView(R.layout.activity_main);
        }
        mStartActivityDetection = (Button) findViewById(R.id.startDataCollection);
        mStopActivityDetection = (Button) findViewById(R.id.stopDataCollection);

        mStartActivityDetection.setOnClickListener(this);
        mStopActivityDetection.setOnClickListener(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        mHistoryFragment = (ActivityHistoryFragment) getFragmentManager().findFragmentById(R.id.activity_history_fragment);
        mThreadScheduler = Executors.newSingleThreadScheduledExecutor();

        writeDir = getDataStorageDir("AccelerometerData");
        writeFile = new File(writeDir, "data.txt");

        /**
         * open file for reading, get prior activities for the fragment to display on UI
         * NOTE the file could be quite long. May want to use writeFile.getFreeSpace() to find out how many bytes
         *      are available to use or come up with some method for reducing the file size eventually.
         */
        try {
            FileReader fr = new FileReader(writeFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                String startTime = tokens[0];
                String startTimeOfDay = tokens[1];
                String dash = tokens[2];
                String endTime = tokens[3];
                String endTimeOfDay = tokens[4];
                String dateRange = startTime + " " + startTimeOfDay + " " + dash + " " + endTime + " " + endTimeOfDay;
                HistoryItem.UserActivity currentActivity = HistoryItem.UserActivity.valueOf(tokens[5].toUpperCase());
                Drawable currentIcon = getUserActivityIcon(currentActivity);
                HistoryItem item = new HistoryItem(currentIcon, currentActivity, dateRange);
                mHistoryFragment.addNewHistoryActivity(item);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fw = new FileWriter(writeFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "BYTES AVAILAbLE TO WRITE: " + writeFile.getFreeSpace());


        mPollUserActivity = new Runnable() {

            @Override
            public void run() {
                try {
                    Date pollEndTime = new Date(System.currentTimeMillis());
                    UserActivity activity = mServiceobj.getSampledUserActivity();
                    Drawable icon = getUserActivityIcon(activity);
                    HistoryItem item = new HistoryItem(icon, activity, mPollStartTime, pollEndTime);
                    mHistoryFragment.addNewHistoryActivity(item);
                    mPollStartTime = pollEndTime;
                    fw.write(item.getDisplayTimeRange() + " " + activity.toString() + "\n");
                    fw.flush();

                } catch (Exception e) {
                    Log.d(TAG, "Polling activity Exception: " + e.toString());
                }
            }
        };

        // We want to keep the screen on since the activity relies on polling
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Used to post to UI
        mHandler = new Handler();

        mBound = false;
        mDetecting = false;


    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public File getDataStorageDir(String fileName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), fileName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory in downloads not created");
        }
        return file;
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
            stopActivityDetection();
            unbindSensorService();
            mBound = false;
            mDetecting = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume'd");
        if( !mBound ) {
            bindSensorService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop'd");
         //Unbind from the service
        if (mBound) {
            stopActivityDetection();
            unbindSensorService();
            mBound = false;
            mDetecting = false;
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
        mServiceobj.stopDataCollection();
        mServiceobj.removeListener(this);
        unbindService(mConnection);
    }

    private void startActivityDetection() {
        mServiceobj.addListener(this);
        mServiceobj.startDataCollection();
        mPollStartTime = new Date(System.currentTimeMillis());
        mActivityPoller = mThreadScheduler.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                   public void run() {
                       mHandler.post(mPollUserActivity);
                   }
               },
                POLLING_INTERVAL_MINUTES,
                POLLING_INTERVAL_MINUTES,
                TimeUnit.MINUTES);

    }

    private void stopActivityDetection() {
        mServiceobj.removeListener(this);
        mServiceobj.stopDataCollection();
        if( mActivityPoller != null ) {
            mActivityPoller.cancel(true);
        }

        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    @Override
    public void onSensorDataChanged(Bundle data) {

        if(DEBUG) {
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

    private Drawable getUserActivityIcon(UserActivity activity) {
        Resources res = getResources();
        switch(activity) {
            case SITTING:
                return res.getDrawable(R.drawable.ic_sit);
            case WALKING:
                return res.getDrawable(R.drawable.ic_walk);
            case SLEEPING:
                return res.getDrawable(R.drawable.ic_sleep);
        }
        return null;
    }

}
