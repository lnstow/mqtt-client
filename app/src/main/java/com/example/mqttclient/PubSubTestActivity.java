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

public class PubSubTestActivity extends AppCompatActivity implements MqttService.MqttEventCallBack {

    private EditText topicPublish, topicSubscribe, messagePublish;
    private TextView connectState, messaageRecv;
    private MqttService.MqttBinder mqttBinder;
    private String TAG = "PubSubTestActivity";
    private String lastSubscribeTopic = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder)iBinder;
            mqttBinder.setMqttEventCallback(PubSubTestActivity.this);
            if(mqttBinder.isConnected()){
                connectState.setText("已连接");
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
        setContentView(R.layout.activity_pub_sub_test);
        topicPublish = findViewById(R.id.topic_publish_et);
        topicSubscribe = findViewById(R.id.topic_sub_et);
        messagePublish = findViewById(R.id.message_publish_et);
        messaageRecv = findViewById(R.id.message_recv_tv);
        connectState = findViewById(R.id.pubsub_connect_state);

        Intent mqttServiceIntent = new Intent(this, MqttService.class);
        bindService(mqttServiceIntent, connection, Context.BIND_AUTO_CREATE);

        findViewById(R.id.pubsub_publish_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topic = topicPublish.getText().toString();
                if("".equals(topic)){
                    Toast.makeText(PubSubTestActivity.this, "主题不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    mqttBinder.publishMessage(topic, messagePublish.getText().toString());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.pubsub_ubscribe_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subscribeNewTopic();
            }
        });
    }

    void subscribeNewTopic(){
        String topic = topicSubscribe.getText().toString();
        if("".equals(topic)){
            Toast.makeText(PubSubTestActivity.this, "主题不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if(lastSubscribeTopic!=null){
            try {
                mqttBinder.unSubscribe(lastSubscribeTopic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        lastSubscribeTopic = null;
        try {
            mqttBinder.subscribe(topic);
            lastSubscribeTopic = topic;
            Toast.makeText(PubSubTestActivity.this, "订阅成功", Toast.LENGTH_SHORT).show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectSuccess() {
        connectState.setText("已连接");
    }

    @Override
    public void onConnectError(String error) {
        Log.d(TAG, "onConnectError: "+error);
        connectState.setText("未连接");
        lastSubscribeTopic = null;
    }

    @Override
    public void onDeliveryComplete() {
        Log.d(TAG, "publish ok");
    }

    @Override
    public void onMqttMessage(String topic, String message) {
        messaageRecv.setText("topic:"+topic+", message:"+message);
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);
        super.onDestroy();
    }
}
