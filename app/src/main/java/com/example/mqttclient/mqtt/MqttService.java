package com.example.mqttclient.mqtt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttService extends Service {

    public static final String TAG = MqttService.class.getSimpleName();
    private static MqttAndroidClient client;
    private MqttEventCallBack mqttEventCallBack;
    private MqttBinder mqttBinder = new MqttBinder();
    MqttConnectOptions conOpt = new MqttConnectOptions();

    @Override
    public void onCreate() {
        super.onCreate();
        connect(MqttParametersManager.readConfig(MqttService.this));
    }

    public interface MqttEventCallBack {
        void onConnectSuccess();

        void onConnectError(String error);

        void onDeliveryComplete();

        void onMqttMessage(String topic, String message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mqttBinder;
    }

    public class MqttBinder extends Binder {

        public void reConnect() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connect(MqttParametersManager.readConfig(MqttService.this));
                }
            }).start();
        }

        public boolean isConnected() {
            return client != null && client.isConnected();
        }

        public void subscribe(String topic) throws MqttException {
            if (client != null && client.isConnected()) {
                client.subscribe(topic, 1);
            }
        }

        public void unSubscribe(String topic) throws MqttException {
            if (client != null && client.isConnected()) {
                client.unsubscribe(topic);
            }
        }

        public void publishMessage(String topic, String message) throws MqttException {
            if (client != null && client.isConnected()) {
                client.publish(topic, message.getBytes(), 0, false);
            }
        }

        public void setMqttEventCallback(MqttEventCallBack callback) {
            mqttEventCallBack = callback;
        }
    }

    private void connect(MqttParameters parameters) {
        disConnect();

        client = new MqttAndroidClient(this, parameters.getUri(), parameters.clientId);
        client.setCallback(mqttCallback);// 设置MQTT监听并且接受消息
        conOpt.setCleanSession(true);// 清除缓存
        conOpt.setConnectionTimeout(10);// 设置超时时间，单位：秒
        conOpt.setKeepAliveInterval(20);// 心跳包发送间隔，单位：秒
        if ((parameters.userName != null) && (!parameters.userName.trim().equals(""))
                && (parameters.passWord != null) && (!parameters.passWord.trim().equals(""))) {
            conOpt.setUserName(parameters.userName);
            conOpt.setPassword(parameters.passWord.toCharArray());
        }

        String lwtMessage = "{\"terminal_uid\":\"" + parameters.clientId + "\"}";
        try {
            conOpt.setWill(parameters.clientId, lwtMessage.getBytes(), 0, false);
        } catch (Exception e) {
            iMqttActionListener.onFailure(null, e);
        }

        try {
            if(!client.isConnected()){
                client.connect(conOpt, null, iMqttActionListener);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void disConnect() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        stopSelf();
        disConnect();
        super.onDestroy();
    }

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "connect success");
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onConnectSuccess();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            Log.i(TAG, "connect fail:" + arg1.toString());
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onConnectError(arg1.toString());
            }
            mqttCallback.connectionLost(arg1);
        }
    };

    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String str1 = new String(message.getPayload());
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onMqttMessage(topic, str1);
            }
            Log.i(TAG, "messageArrived:" + str1);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onDeliveryComplete();
            }
        }

        @Override
        public void connectionLost(Throwable arg0) {
            if (mqttEventCallBack != null) {
                mqttEventCallBack.onConnectError("Connecting lost! MqttService will reconnect after 5s...");
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (client != null && !client.isConnected()) {
                try {
                    client.connect(conOpt, null, iMqttActionListener);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static boolean isNetworkReady(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }
}
