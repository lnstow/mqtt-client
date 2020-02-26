package com.example.mqttclient;

import android.content.Context;
import android.content.SharedPreferences;

public class MqttParametersManager {
    public static void saveConfig(Context context, MqttParameters parameters){
        SharedPreferences.Editor editor = context.getSharedPreferences("MqttParameters", Context.MODE_PRIVATE).edit();
        editor.putString("uri", parameters.uri);
        editor.putString("clientId", parameters.clientId);
        editor.putString("userName", parameters.userName);
        editor.putString("passWord", parameters.passWord);
        editor.apply();
    }

    public static MqttParameters readConfig(Context context){
        SharedPreferences preferences = context.getSharedPreferences("MqttParameters", Context.MODE_PRIVATE);
        return new MqttParameters(preferences.getString("uri", "")
        ,preferences.getString("clientId", "")
        ,preferences.getString("userName", "")
        ,preferences.getString("passWord", ""));
    }
}
