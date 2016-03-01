package com.nlutz.weatherapp;

import org.json.JSONObject;

/**
 * Created by nick on 2/29/16.
 */
public interface AsyncTasker {
    void onTaskStarted();
    void onTaskCompleted(JSONObject results);
}
