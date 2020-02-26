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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mqttclient.mqtt.MqttService;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity implements MqttService.MqttEventCallBack {

    private TextView tv, connectState;
    private EditText et;
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

        tv = findViewById(R.id.text);
        et = findViewById(R.id.input);
        connectState = findViewById(R.id.connect_state);

        final Intent intent = new Intent(this, MqttService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.settings_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttBinder.publishMessage("/test", et.getText().toString());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.connect_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    void subscribeTopics(){
        try {
            Toast.makeText(MainActivity.this, "connect ok", Toast.LENGTH_SHORT).show();
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
        //Toast.makeText(MainActivity.this, "publish ok", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "publish ok");
    }

    @Override
    public void onMqttMessage(String topic, String message) {
        tv.setText(topic+":"+message);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        subscribeTopics();
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
