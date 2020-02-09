package net.speleomaniac.sensorforward;

import android.content.Context;
import android.hardware.Sensor;

import java.util.ArrayList;

import android.hardware.SensorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class SensorAdapter extends ArrayAdapter<SensorItem> {

    private MainActivity Context;

    public SensorAdapter(Context context, ArrayList<SensorItem> sensors) {
        super(context, 0, sensors);
        Context = (MainActivity)context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final SensorItem sensor = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.sensor_listitem, parent, false);
        }
        // Lookup view for data population
        TextView sensorName = convertView.findViewById(R.id.sensorName);
        CheckBox sensorRegistered = convertView.findViewById(R.id.sensorCheck);
        sensorRegistered.setTag(position);
        // Populate the data into the template view using the data object
        sensorName.setText(sensor.Name);
        sensorRegistered.setChecked(sensor.Registered);

        sensorRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (Integer) view.getTag();
                SensorItem item = getItem(position);
                item.Registered = !item.Registered;
                if (item.Registered)
                    Context.sensorManager.registerListener(Context, item.Sensor, SensorManager.SENSOR_DELAY_FASTEST);
                else
                    Context.sensorManager.unregisterListener(Context, item.Sensor);

            }
        });
        // Return the completed view to render on screen
        return convertView;
    }
}
