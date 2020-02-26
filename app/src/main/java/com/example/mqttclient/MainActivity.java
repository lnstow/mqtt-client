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

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MainActivity extends AppCompatActivity implements MqttService.MqttMessageCallBack {

    private TextView textView;
    private EditText input;
    private MqttService.MqttBinder mqttBinder;
    private String TAG = "MainActivity";

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder)iBinder;
            mqttBinder.setMessageCallback(MainActivity.this);
            if(mqttBinder.isConnected()){
                //subscribe topics
            } else {
                mqttBinder.startConnectMqttServer(iMqttActionListener, MqttParametersManager.readConfig(MainActivity.this));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            //subscribe topics
            mqttBinder.subscribe("/test");
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            Log.i(TAG, "connect fail:"+arg1.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        input = findViewById(R.id.input);

        final Intent intent = new Intent(this, MqttService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttBinder.publishMessage("/test", input.getText().toString());
            }
        });
    }

    @Override
    public void onMqttMessage(String message) {
        textView.setText(message);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }

}
