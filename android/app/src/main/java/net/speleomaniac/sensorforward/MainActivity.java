package net.speleomaniac.sensorforward;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public SensorManager sensorManager;
    private List<Sensor> sensors;
    private ListView sensorList;
    private ArrayList<SensorItem> sensorArray = new ArrayList<>();
    private SensorAdapter sensorAdapter;
    private BufferedWriter bufferedWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        sensorList = findViewById(R.id.sensorList);
        sensorAdapter = new SensorAdapter(this, sensorArray);
        sensorList.setAdapter(sensorAdapter);
        for (int i=0; i<sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            SensorItem item = new SensorItem();
            item.Name = sensor.getName();
            item.Sensor = sensor;
            item.Registered = false;
            sensorArray.add(item);
        }
        sensorAdapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT > 23){
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 1);
        }
        String path = getExternalFilesDir(null) + "/log.file";

        File logFile = new File(path);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try
        {
            bufferedWriter.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    public void onSensorChanged(SensorEvent sensorEvent) {

        String s = "STATION\t" + sensorEvent.timestamp + "\t" + sensorEvent.sensor.getName();
        for (int i=0; i<sensorEvent.values.length; i++) {
            s += "\t" + sensorEvent.values[i];
        }
        AppendToLog(s);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void AppendToLog(String text)
    {
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            bufferedWriter.append(text);
            bufferedWriter.newLine();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
