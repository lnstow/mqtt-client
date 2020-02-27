package com.example.mqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mqttclient.mqtt.MqttService;
import com.example.mqttclient.protocol.AirConditioningMessage;
import com.example.mqttclient.protocol.BoolMessage;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttException;

public class DevicesDemoActivity extends AppCompatActivity implements MqttService.MqttEventCallBack, CompoundButton.OnCheckedChangeListener {

    private TextView connectState;
    private EditText airCconditioningValue;
    private MqttService.MqttBinder mqttBinder;
    private String TAG = "MainActivity";
    private Switch parlourLightSwitch, curtain_switch, fan_socket_switch, air_conditioning_switch;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder) iBinder;
            mqttBinder.setMqttEventCallback(DevicesDemoActivity.this);
            if (mqttBinder.isConnected()) {
                connectState.setText("已连接");
                subscribeTopics();
            } else {
                connectState.setText("未连接");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_demo);

        connectState = findViewById(R.id.dev_connect_state);

        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);

        airCconditioningValue = findViewById(R.id.air_conditioning_value);

        parlourLightSwitch = findViewById(R.id.parlour_light_switch);
        parlourLightSwitch.setOnCheckedChangeListener(this);
        curtain_switch = findViewById(R.id.curtain_switch);
        curtain_switch.setOnCheckedChangeListener(this);
        fan_socket_switch = findViewById(R.id.fan_socket_switch);
        fan_socket_switch.setOnCheckedChangeListener(this);
        air_conditioning_switch = findViewById(R.id.air_conditioning_switch);
        air_conditioning_switch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.parlour_light_switch:
                try {
                    if (compoundButton.isChecked()) {
                        mqttBinder.publishMessage("/test/light1",
                                new Gson().toJson(new BoolMessage(true)));
                    } else {
                        mqttBinder.publishMessage("/test/light1",
                                new Gson().toJson(new BoolMessage(false)));
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.curtain_switch:
                try {
                    if (compoundButton.isChecked()) {
                        mqttBinder.publishMessage("/test/curtain1",
                                new Gson().toJson(new BoolMessage(true)));
                    } else {
                        mqttBinder.publishMessage("/test/curtain1",
                                new Gson().toJson(new BoolMessage(false)));
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.fan_socket_switch:
                try {
                    if (compoundButton.isChecked()) {
                        mqttBinder.publishMessage("/test/fan1",
                                new Gson().toJson(new BoolMessage(true)));
                    } else {
                        mqttBinder.publishMessage("/test/fan1",
                                new Gson().toJson(new BoolMessage(false)));
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.air_conditioning_switch:
                try {
                    if (compoundButton.isChecked()) {
                        String json = new Gson().toJson(new AirConditioningMessage(true,
                                Float.parseFloat(airCconditioningValue.getText().toString())));
                        Log.d("json",json);
                        mqttBinder.publishMessage("/test/airConditioning",json);
                    } else {
                        String json = new Gson().toJson(new AirConditioningMessage(false,
                                Float.parseFloat(airCconditioningValue.getText().toString())));
                        Log.d("json",json);
                        mqttBinder.publishMessage("/test/airConditioning",json);
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    void subscribeTopics() {
        try {
            mqttBinder.subscribe("/test/temp");
            mqttBinder.subscribe("/test/hum");
            mqttBinder.subscribe("/test/pm");
            mqttBinder.subscribe("/test/gas");
            mqttBinder.subscribe("/test/door");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void unSubscribeTopics() {
        try {
            mqttBinder.unSubscribe("/test/temp");
            mqttBinder.unSubscribe("/test/hum");
            mqttBinder.unSubscribe("/test/pm");
            mqttBinder.unSubscribe("/test/gas");
            mqttBinder.unSubscribe("/test/door");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectSuccess() {
        subscribeTopics();
        connectState.setText("已连接");
    }

    @Override
    public void onConnectError(String error) {
        Log.d(TAG, "onConnectError: " + error);
        connectState.setText("未连接");
    }

    @Override
    public void onDeliveryComplete() {
        Log.d(TAG, "publish ok");
    }

    @Override
    public void onMqttMessage(String topic, String message) {
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mqttBinder.isConnected()) {
            connectState.setText("已连接");
            subscribeTopics();
        } else {
            connectState.setText("未连接");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribeTopics();
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

}
