package net.speleomaniac.sensorforward;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private BufferedWriter bufferedWriter;
    private Button btnStartStop;
    private Button btnSensors;
    private Boolean IsRunning = false;
    private TextView txtUDP;
    private TextView txtStation;
    private String StationName;
    private CheckBox chkLog;
    private DatagramSocket socket;
    private InetAddress BroadcastAddress = null;
    private int BroadcastPort = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        txtUDP = findViewById(R.id.txtUDP);
        txtStation = findViewById(R.id.txtStation);
        chkLog = findViewById(R.id.chkLog);

        btnSensors = findViewById(R.id.btnSensors);
        btnSensors.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view) {
                Intent sensorIntent = new Intent(getApplicationContext(), SensorListActivity.class);
                startActivity(sensorIntent);
                return true;
            }
        });


        btnStartStop = findViewById(R.id.btnStartStop);
        btnStartStop.setText(R.string.start);
        btnStartStop.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view) {
            {
                IsRunning = !IsRunning;
                btnStartStop.setText(IsRunning? R.string.stop : R.string.start);
                chkLog.setEnabled(!IsRunning);
                txtStation.setEnabled(!IsRunning);
                txtUDP.setEnabled(!IsRunning);
                btnSensors.setEnabled(!IsRunning);
                StartStop();
            }
            return true;
        }});

        if (Build.VERSION.SDK_INT > 23){
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1);
            requestPermissions(new String[]{INTERNET}, 1);
            requestPermissions(new String[]{ACCESS_NETWORK_STATE}, 1);
        }


        SharedPreferences prefs = getSharedPreferences("settings", 0);
        txtUDP.setText(prefs.getString("udp", ""));
        txtStation.setText(prefs.getString("station", ""));
        chkLog.setChecked(prefs.getBoolean("log", false));
    }

    private void StartStop() {
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        boolean writeLog = chkLog.isChecked();
        String UDPDest = txtUDP.getText().toString();
        StationName = txtStation.getText().toString();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("udp", UDPDest);
        editor.putString("station", StationName);
        editor.putBoolean("log", writeLog);
        editor.apply();


        prefs = getSharedPreferences("sensors", 0);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (int i=0; i<sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            if (prefs.getBoolean(sensor.getName(), false)) {
                if (IsRunning)
                    sensorManager.registerListener(MainActivity.this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
                else
                    sensorManager.unregisterListener(MainActivity.this, sensor);
            }
        }
        if (IsRunning) {
            String[] parts = UDPDest.split(":");
            if (parts.length == 2) {


                try {
                    BroadcastAddress = InetAddress.getByName(parts[0]);
                    BroadcastPort = Integer.parseInt(parts[1]);
                    socket = new DatagramSocket(BroadcastPort);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                }


            }
            else {
                socket = null;
            }

            if (writeLog) {
                String path = getExternalFilesDir(null) + "/log.file";
                File logFile = new File(path);
                if (!logFile.exists())
                {
                    try
                    {
                        logFile.createNewFile();
                        try
                        {
                            bufferedWriter = new BufferedWriter(new FileWriter(logFile, true));
                        }
                        catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    catch (IOException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        else if (bufferedWriter != null) {
            try
            {
                bufferedWriter.close();
                bufferedWriter = null;
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        }
    }


    @Override
    protected void onDestroy() {
        IsRunning = false;
        StartStop();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        StringBuilder s = new StringBuilder();
        s.append(StationName).append("\t").append(sensorEvent.timestamp).append("\t").append(sensorEvent.sensor.getName());
        for (int i=0; i<sensorEvent.values.length; i++) {
            s.append("\t").append(sensorEvent.values[i]);
        }
        s.append("\n");
        ProcessData(s.toString());


        int type = sensorEvent.sensor.getType();
        if (type == Sensor.TYPE_ROTATION_VECTOR ||
                type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR ||
                type == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            s.setLength(0);
            float[] matrix = new float[9];
            SensorManager.getRotationMatrixFromVector(matrix, sensorEvent.values);
            s.append(StationName).append("\t").append(sensorEvent.timestamp).append("\t").append(sensorEvent.sensor.getName()).append("-MATRIX");
            for (float v : matrix) {
                s.append("\t").append(v);
            }
            s.append("\n");
            ProcessData(s.toString());
        }
    }

    private void ProcessData(String data) {
        if (bufferedWriter != null) {
            try
            {
                bufferedWriter.append(data);
            }
            catch (IOException e)
            {
                //e.printStackTrace();
            }
        }

        if (socket != null) {
            DatagramPacket p = new DatagramPacket(data.getBytes(), data.length(), BroadcastAddress, BroadcastPort);
            try {
                socket.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
