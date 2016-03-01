package com.nlutz.weatherapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AsyncTasker {
    Map<Integer, String> favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        favorites = new HashMap<>();
        prePopulateLocations();
    }

    public void getWeatherByZip(View v) {
        EditText et = (EditText) findViewById(R.id.search_field);
        String text = et.getText().toString();
        Integer zip;
        try {
            zip = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            Log.e("MainActivity", "Unknown zip code " + text);
            Toast.makeText(getBaseContext(), String.format("Unknown zip code %s", text),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("MainActivity", "zip: " + zip);
        Integer[] params = {zip};
        new WeatherClient(this).execute(params);
    }

    public void locationListener(View v) {
        View location = (View) v.getParent();
        Log.d("MainActivity", "zip: " + location.getId());
        Integer[] params = {location.getId()};
        new WeatherClient(this).execute(params);
    }

    public void deleteLocation(View v) {
        View parent = (View) v.getParent();
        Log.d("MainActivity", "Removing " + parent.getId());
        favorites.remove(parent.getId());
        LinearLayout locationList = (LinearLayout) v.getParent().getParent();
        locationList.removeView(parent);
    }

    private void prePopulateLocations() {
        Map<Integer, String> zips = new HashMap<>();
        zips.put(78757, "Travis County");
        zips.put(95124, "Cambrian Park");
        zips.put(92024, "Encinitas");

        for (Map.Entry<Integer, String> entry : zips.entrySet()) {
            addFavoriteLocation(entry.getKey(), entry.getValue());
        }
    }

    private void addFavoriteLocation(int zip, String city) {
        if (favorites.get(zip) == null) {
            favorites.put(zip, city);
            Log.d("MainActivity", "Adding location " + zip + " " + city);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout locations = (LinearLayout) findViewById(R.id.locations_list);
            View location = inflater.inflate(R.layout.location_item, locations, false);
            TextView name = (TextView) location.findViewById(R.id.button_location_name);
            Resources res = getResources();
            name.setText(String
                    .format(res.getString(R.string.location), city, zip));
            location.setId(zip);
            Log.d("MainActivity", "location id " + location.getId());
            locations.addView(location);
        } else {
            Toast.makeText(getBaseContext(),
                    String.format("%s is already in your favorites", city),
                    Toast.LENGTH_LONG).show();
        }
    }

    private AlertDialog buildDetailsDialog(final JSONObject results) {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup parent = (ViewGroup) findViewById(R.id.locations_list);
        View details = inflater.inflate(R.layout.layout_details_dialog, parent, false);
        TextView tempView = (TextView) details.findViewById(R.id.temperature);
        TextView humidityView = (TextView) details.findViewById(R.id.humidity);
        TextView descView = (TextView) details.findViewById(R.id.weather_desc);
        TextView windView = (TextView) details.findViewById(R.id.wind);
        try {
            String name = (String) results.get("name");
            int zip = (int) results.get("zip");
            ab.setTitle(String.format("%s (%d)", name, zip));
            JSONObject desc = ((JSONArray) results.get("weather")).getJSONObject(0);
            descView.setText((String) desc.get("description"));
            JSONObject weather = (JSONObject) results.get("main");
            try {
                tempView.setText(String
                        .format("Current Temperature: %.1f", (Double) weather.get("temp")));
            } catch (ClassCastException e) {
                // Temp may come back as a double or a float
                tempView.setText(String
                        .format("Current Temperature: %d", (int) weather.get("temp")));
            }
            JSONObject wind = (JSONObject) results.get("wind");
            try {
                int speed = (int) wind.get("speed");
                windView.setText(String.format("Wind Speed: %d", speed));
            } catch (ClassCastException e) {
                Double speed = (Double) wind.get("speed");
                windView.setText(String.format("Wind Speed: %.1f", speed));
            }
            humidityView.setText(String.format("Humidity: %d", (Integer) weather.get("humidity")));
        } catch (JSONException e) {
            Log.e("WeatherClient",
                    String.format("Error parsing weather data %s",results.toString()));
            Toast.makeText(getBaseContext(),
                    String.format("Error parsing weather data %s",results.toString()),
                    Toast.LENGTH_LONG).show();
        }
        ab.setView(details);
        ab.setCancelable(false);

        ab.setNegativeButton("Back", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        ab.setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    addFavoriteLocation((int) results.get("zip"), (String) results.get("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return ab.create();
    }

    public void onTaskStarted() {
    }

    public void onTaskCompleted(final JSONObject results) {
        try {
            Log.d("MainActivity", "cod " + results.get("cod"));
            if (results.get("cod").equals("404")) {
                Toast.makeText(getBaseContext(), "Unknown zip code " + results.get("zip"),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AlertDialog detailsDialog = buildDetailsDialog(results);
        detailsDialog.show();
    }

}
