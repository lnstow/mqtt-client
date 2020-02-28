package com.example.mqttclient.mqtt;

import android.content.Context;
import android.content.SharedPreferences;

public class MqttParametersManager {

    public static void saveConfig(Context context, MqttParameters parameters) {
        SharedPreferences.Editor editor = context.getSharedPreferences("MqttParameters", Context.MODE_PRIVATE).edit();
        editor.putString("serverIp", parameters.serverIp);
        editor.putString("port", parameters.port);
        editor.putString("clientId", parameters.clientId);
        editor.putString("userName", parameters.userName);
        editor.putString("passWord", parameters.passWord);
        editor.apply();
    }

    public static MqttParameters readConfig(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MqttParameters", Context.MODE_PRIVATE);
        return new MqttParameters(preferences.getString("serverIp", "192.168.1.101")
                , preferences.getString("port", "1883")
                , preferences.getString("clientId", "clientAndroid")
                , preferences.getString("userName", "")
                , preferences.getString("passWord", ""));
    }
}
