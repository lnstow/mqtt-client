package com.example.mqttclient;

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
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MqttService extends Service {

    public static final String TAG = MqttService.class.getSimpleName();
    private static MqttAndroidClient client;
    private String host = "tcp://192.168.1.103:1883";
    private MqttMessageCallBack mqttMessageCallBack;
    private MqttBinder mqttBinder = new MqttBinder();
    private boolean startConnectFlag = false;
    MqttConnectOptions conOpt = new MqttConnectOptions();

    public MqttService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(getClass().getName(), "onCreate");
    }

    public interface MqttMessageCallBack {
        void onMqttMessage(String message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(getClass().getName(), "onBind");
        return mqttBinder;
    }

    class MqttBinder extends Binder {
        public void startConnectMqttServer(IMqttActionListener iMqttActionListener, MqttParameters parameters) {
            startConnectFlag = true;
            connect(iMqttActionListener, parameters);
        }

        public void disConnectMqttServer() {
            startConnectFlag = false;
            disConnect();
        }

        public boolean isConnected() {
            return client != null && client.isConnected();
        }

        public boolean subscribe(String topic) {
            try {
                client.subscribe(topic, 1);
                return true;
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return false;
        }

        public boolean publishMessage(String topic, String message) {
            try {
                if (client != null) {
                    client.publish(topic, message.getBytes(), 0, false);
                    return true;
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
            return false;
        }

        public void setMessageCallback(MqttMessageCallBack callback) {
            mqttMessageCallBack = callback;
        }
    }

    private void connect(IMqttActionListener iMqttActionListener, MqttParameters parameters) {
        if (!isNetworkReady()) {
            return;
        }

        if (client == null) {
            // uri:协议+地址+端口号
            client = new MqttAndroidClient(this, parameters.uri, parameters.clientId);
            client.setCallback(mqttCallback);// 设置MQTT监听并且接受消息
        }

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
            client.connect(conOpt, null, iMqttActionListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void disConnect() {
        if (client != null) {
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

    // MQTT监听并且接受消息
    private MqttCallback mqttCallback = new MqttCallback() {

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            String str1 = new String(message.getPayload());
            if (mqttMessageCallBack != null) {
                mqttMessageCallBack.onMqttMessage(str1);
            }
            String str2 = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
            Log.i(TAG, "messageArrived:" + str1);
            Log.i(TAG, str2);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {

        }

        @Override
        public void connectionLost(Throwable arg0) {
            // 失去连接，重连
        }
    };

    /**
     * 判断网络是否连接
     */
    private boolean isNetworkReady() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.i(TAG, "MQTT当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "MQTT 没有可用网络");
            return false;
        }
    }


}
