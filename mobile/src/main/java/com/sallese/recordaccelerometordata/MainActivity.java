package com.sallese.recordaccelerometordata;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener{

    float x;
    float y;
    float z;
    long timeSent;
    float[] accData = new float[3];
    TextView messageData;
    long timeNow;
    Date timeNowDate;
    Date timeSentDate;
    float[] gravity = new float[3];
    private GoogleApiClient mGoogleApiClient;
    private String TAG = "debugWear";
    Button buttonRecord;
    boolean recordEnabled;
    FileWriter recorder;
    EditText et_filename;
    EditText et_reps;
    EditText et_activity;
    EditText et_weight;

    FileOutputStream outputStream;
    File sdCard = Environment.getExternalStorageDirectory();
    FileOutputStream f;
    PrintWriter pw;
    File file;
    Notification notification;
    String notificationActionText;
    private PowerManager.WakeLock wl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        messageData = (TextView) findViewById(R.id.messageData);
        buttonRecord = (Button) findViewById(R.id.buttonRecord);
        et_filename = (EditText) findViewById(R.id.ET_filename);
        et_reps = (EditText) findViewById(R.id.ET_reps);
        et_activity = (EditText) findViewById(R.id.ET_activity);
        et_weight = (EditText) findViewById(R.id.ET_weight);
        recordEnabled = false;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (recordEnabled == true){
            notificationActionText = "Stop recording";
        }
        else{
            notificationActionText = "Start recording";
        }

        Intent intent = new Intent(this, com.sallese.recordaccelerometordata.MainActivity.class);
        intent.putExtra("methodName","clickRecord");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_cast_dark, notificationActionText, pendingIntent).build();

        notification = new NotificationCompat.Builder(this)
                .setContentText("HELLO WORLD")
                .setContentTitle("HI World")
                .setSmallIcon(R.drawable.ic_cast_dark)
//                .setOngoing(true)
                .extend(new NotificationCompat.WearableExtender().addAction(action))
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(001,notification);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.getStringExtra("methodName").equals("clickRecord")){
            clickRecord();
        }
    }

    public void clickRecord(){
//        buttonRecord.performClick();
        Toast.makeText(MainActivity.this, "It worked!", Toast.LENGTH_SHORT).show();
        if (recordEnabled == true){
            notificationActionText = "Stop recording";
        }
        else{
            notificationActionText = "Start recording";
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("methodName","clickRecord");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_cast_dark, notificationActionText, pendingIntent).build();

        notification = new NotificationCompat.Builder(this)
                .setContentText("HELLO WORLD")
                .setContentTitle("HI World")
                .setSmallIcon(R.drawable.ic_cast_dark)
//                .setOngoing(true)
                .extend(new NotificationCompat.WearableExtender().addAction(action))
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(001, notification);

    }


    public void recordEvent(View v) {
        if (recordEnabled == false){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                File root = android.os.Environment.getExternalStorageDirectory();
                String dateTag = (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime())).toString();
                File dir = new File(root.getAbsolutePath() + "/AccelData");
                String fileName = et_filename.getText().toString() + dateTag + ".txt";
                String activityText = et_activity.getText().toString();
                String weightText = et_weight.getText().toString();
                String repsText = et_reps.getText().toString();
                dir.mkdirs();
                wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");
                wl.acquire();
                file = new File(dir, fileName);
            try {
                f = new FileOutputStream(file);
                pw = new PrintWriter(f);
                pw.println(activityText);
                pw.println(repsText);
                if (!weightText.isEmpty()){
                    pw.println(weightText);
                }
                pw.println("x,y,z");
                pw.flush();
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
            recordEnabled = true;
            buttonRecord.setText("Stop Recording");
        }
        else{
            recordEnabled = false;
            wl.release();
            try {
                if(f != null) {
                    f.close();
                }
                if (pw != null){
                    pw.flush();
                    pw.close();
                }
            }
            catch (IOException e){
                throw new RuntimeException(e);
            }
            buttonRecord.setText("Start Recording");
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
    public void onMessageReceived(final MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase("/accMessage")) {
            String str;
            try {
                str = new String(messageEvent.getData(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("UTF-8 is unknown");
            }
            String[] accXYZString = str.split("_");
            x = Float.parseFloat(accXYZString[0]);
            y = Float.parseFloat(accXYZString[1]);
            z = Float.parseFloat(accXYZString[2]);
            timeSent = Long.parseLong(accXYZString[3]);

            accData[0] = x;
            accData[1] = y;
            accData[2] = z;

            final float alpha = 0.8f;
            gravity[0] = alpha * gravity[0] + (1 - alpha) * accData[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * accData[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * accData[2];

            x = accData[0] - gravity[0];
            y = accData[1] - gravity[1];
            z = accData[2] - gravity[2];

            runOnUiThread(new Runnable() {
                public void run() {
                    messageData.setText("X: " + x + "\n" + "Y: " + y + "\n" + "Z: " + z);
                }
            });
            setUpAccData(accData);
        }
        else if (messageEvent.getPath().equalsIgnoreCase("/startRecord")){
            recordEnabled = true;
        }
    }

    protected void onResume() {
        super.onResume();
//        try {
//            f = new FileOutputStream(file);
//            pw = new PrintWriter(f);
//        }
//        catch (IOException e){
//            throw new RuntimeException(e);
//        }
    }

    protected void onPause() {
        super.onPause();
//        try {
//            if(f != null) {
//                pw.flush();
//                pw.close();
//                f.close();
//            }
//            recordEnabled = false;
//        }
//        catch (IOException e){
//            throw new RuntimeException(e);
//        }
    }

    public void setUpAccData(float[] accData) {
        timeNow = System.currentTimeMillis();
        timeNowDate = new Date(timeNow);
        timeSentDate = new Date(timeSent);
//        Log.d("Movement","Difference in time: " + (timeNow-timeSent) + "\nTime sent: " + timeSentDate.getMinutes() + ":" + timeSentDate.getSeconds() + "Time recieved: " + timeNowDate.getMinutes() + ":" + timeNowDate.getSeconds());
        final float alpha = 0.8f;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * accData[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * accData[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * accData[2];

        float x = accData[0] - gravity[0];
        float y = accData[1] - gravity[1];
        float z = accData[2] - gravity[2];

        if (recordEnabled == true){
                String dataToWrite = x+","+y+","+z+"\n";
                pw.println(dataToWrite);
                pw.flush();
        }
    }

    public void onConnectionSuspended(int cause){
        Log.d(TAG, "connection suspended");
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Connected to Google Api Service");
        }

//        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
//        sendMessage("/startMainActivity", "dummy");
//        addDataItem();
    }

    public void onConnectionFailed(ConnectionResult connectionResult){
        Log.d(TAG, "connection failed");
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }
}
