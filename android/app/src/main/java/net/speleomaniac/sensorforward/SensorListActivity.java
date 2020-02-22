package net.speleomaniac.sensorforward;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SensorListActivity extends Activity {
    private ArrayList<SensorItem> SensorArray;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_list);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        ListView sensorList = findViewById(R.id.sensorList);
        SensorArray = new ArrayList<>();
        SensorAdapter sensorAdapter = new SensorAdapter(this, SensorArray);
        sensorList.setAdapter(sensorAdapter);

        SharedPreferences prefs = getSharedPreferences("sensors", 0);

        for (int i = 0; i< sensors.size(); i++) {
            Sensor sensor = sensors.get(i);
            SensorItem item = new SensorItem();
            item.ID = sensor.getName();
            item.DisplayName = sensor.getName();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                //noinspection StringConcatenationInLoop
                item.DisplayName += "\n(" + sensor.getStringType() + ")";
            }
            item.Registered = prefs.getBoolean(item.ID, false);
            SensorArray.add(item);
        }
        sensorAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor prefs = getSharedPreferences("sensors", 0).edit();
        for (int i = 0; i< SensorArray.size(); i++){
            prefs.putBoolean(SensorArray.get(i).ID, SensorArray.get(i).Registered);
        }
        prefs.apply();
        super.onDestroy();
    }

}
