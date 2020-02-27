package com.example.mqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.mqttclient.mqtt.MqttService;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity implements MqttService.MqttEventCallBack {

    private TextView connectState;
    private MqttService.MqttBinder mqttBinder;
    private String TAG = "MainActivity";

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder)iBinder;
            mqttBinder.setMqttEventCallback(MainActivity.this);
            if(mqttBinder.isConnected()){
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
        setContentView(R.layout.activity_main);

        connectState = findViewById(R.id.connect_state);

        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.pubsub_test_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PubSubTestActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.dev_demo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DevicesDemoActivity.class);
                startActivity(intent);
            }
        });
    }

    void subscribeTopics(){
        try {
            mqttBinder.subscribe("/test");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    void unSubscribeTopics(){
        try {
            mqttBinder.unSubscribe("/test");
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
        Log.d(TAG, "onConnectError: "+error);
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
        if(mqttBinder.isConnected()){
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
