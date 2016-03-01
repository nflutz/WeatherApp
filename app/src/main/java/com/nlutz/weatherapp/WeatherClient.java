package com.nlutz.weatherapp;


import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class WeatherClient extends AsyncTask<Integer, String, JSONObject>{
    private static final String OPEN_WEATHER_MAP_URL =
            "http://api.openweathermap.org/data/2.5/weather?zip=%d&appid=%s&units=imperial";
    private static final String OPEN_WEATHER_MAP_KEY = "d940bc5de98ecc520093906eadd1f8e3";
    private AsyncTasker listener;

    public WeatherClient(AsyncTasker tasker) {
        this.listener = tasker;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onTaskStarted();
    }

    @Override
    protected void onPostExecute(JSONObject data) {
        super.onPostExecute(data);
        listener.onTaskCompleted(data);
    }

    @Override
    protected JSONObject doInBackground(Integer... params) {
        Integer zip = params[0];
        String url = String.format(OPEN_WEATHER_MAP_URL, zip, OPEN_WEATHER_MAP_KEY);
        String obfuscatedUrl = String.format(OPEN_WEATHER_MAP_URL, zip, "XXXX");
        Log.d("WeatherClient", "Requesting " + obfuscatedUrl);
        JSONObject data = makeRequest(url);
        try {
            if (data.length() != 0) {
                Log.d("WeatherClient", "Received " + data.toString());
            }
            data.put("zip", zip);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    private JSONObject makeRequest(String url) {
        HttpClient client = new DefaultHttpClient();
        HttpGet req = new HttpGet(url);
        InputStream is;
        JSONObject jobj = new JSONObject();
        try {
            HttpResponse resp = client.execute(req);
            HttpEntity entity = resp.getEntity();
            is = entity.getContent();
        } catch (IOException e) {
            e.printStackTrace();
            return jobj;
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                while((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();
                String respBody = sb.toString();
                try {
                    jobj = new JSONObject(respBody);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
         } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return jobj;
    }

}
