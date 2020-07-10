package com.example.mqttclient;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mqttclient.mqtt.MqttParameters;
import com.example.mqttclient.mqtt.MqttParametersManager;
import com.example.mqttclient.mqtt.MqttService;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private MqttService.MqttBinder mqttBinder;
    private String TAG = "SettingsActivity";
    private EditText etServerIp, etPort, etClientId, etUserName, etPassword;
    private List<MqttParameters> mqttList;
    private File mqttFile;
    private Gson gson;
    private FlexboxLayout flexboxLayout;
    private int index;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mqttBinder = (MqttService.MqttBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final Intent intent = new Intent(this, MqttService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        flexboxLayout = findViewById(R.id.set_flex);
        mqttFile = new File(getExternalFilesDir(null), "mqtt");
        gson = new Gson();
        index = -1;
        loadFromFile();

        View view = LayoutInflater.from(this).inflate(R.layout.settings_cardview, flexboxLayout, false);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(view.getLayoutParams());
        params.setOrder(3);
        ImageView imageView = view.findViewById(R.id.set_image);
        TextView textView = view.findViewById(R.id.set_text);
        imageView.setImageResource(R.drawable.ic_add_black_24dp);
        textView.setText("增加设备");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                View dialogView = LayoutInflater.from(v.getContext()).inflate(
                        R.layout.settings_dialog, (ViewGroup) v.getParent(), false);

                etServerIp = dialogView.findViewById(R.id.et_server_ip);
                etPort = dialogView.findViewById(R.id.et_server_port);
                etClientId = dialogView.findViewById(R.id.et_client_id);
                etUserName = dialogView.findViewById(R.id.et_user_name);
                etPassword = dialogView.findViewById(R.id.et_password);
                etServerIp.setText("192.168.1.101");
                etPort.setText("1883");
                etClientId.setText("clientAndroid");
                builder.setPositiveButton("增加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MqttParameters mqttParameters = new MqttParameters(
                                etServerIp.getText().toString()
                                , etPort.getText().toString()
                                , etClientId.getText().toString()
                                , etUserName.getText().toString()
                                , etPassword.getText().toString()
                        );
                        insertFlex(mqttParameters, mqttList.size());
                        updateFlex(mqttList.size());
                        mqttList.add(mqttParameters);
                    }
                });
                builder.setView(dialogView).create().show();
            }
        });
        flexboxLayout.addView(view, params);

        MqttParameters mqttParameters = MqttParametersManager.readConfig(SettingsActivity.this);
        for (int i = 0; i < mqttList.size(); i++) {
            final MqttParameters parameters = mqttList.get(i);
            if (index == -1 && mqttParameters.serverIp.equals(parameters.serverIp)
                    && mqttParameters.port.equals(parameters.port)
                    && mqttParameters.clientId.equals(parameters.clientId))
                index = i;
            insertFlex(parameters, i);
        }

        findViewById(R.id.save_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index != -1)
                    MqttParametersManager.saveConfig(SettingsActivity.this, mqttList.get(index));
                Toast.makeText(SettingsActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                mqttBinder.reConnect();
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        saveToFile();
        unbindService(connection);
        super.onDestroy();
    }

    public void updateFlex(int viewIndex) {
        if (index != -1)
            ((ImageView) flexboxLayout.getChildAt(index + 2).findViewById(R.id.set_image))
                    .setImageResource(R.drawable.ic_close_black_24dp);
        ((ImageView) flexboxLayout.getChildAt(viewIndex + 2).findViewById(R.id.set_image))
                .setImageResource(R.drawable.ic_check_black_24dp);
        index = viewIndex;
    }

    public void deleteFlex(int viewIndex) {
        flexboxLayout.removeViewAt(viewIndex + 2);
        if (index == viewIndex) index = -1;
    }

    public void insertFlex(final MqttParameters parameters, final int viewIndex) {
        View view = LayoutInflater.from(this).inflate(R.layout.settings_cardview, flexboxLayout, false);
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(view.getLayoutParams());
        ImageView imageView = view.findViewById(R.id.set_image);
        TextView textView = view.findViewById(R.id.set_text);

        if (index == viewIndex)
            imageView.setImageResource(R.drawable.ic_check_black_24dp);
        else
            imageView.setImageResource(R.drawable.ic_close_black_24dp);
        textView.setText("设备：" + viewIndex);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                View dialogView = LayoutInflater.from(v.getContext()).inflate(
                        R.layout.settings_dialog, (ViewGroup) v.getParent(), false);

                etServerIp = dialogView.findViewById(R.id.et_server_ip);
                etPort = dialogView.findViewById(R.id.et_server_port);
                etClientId = dialogView.findViewById(R.id.et_client_id);
                etUserName = dialogView.findViewById(R.id.et_user_name);
                etPassword = dialogView.findViewById(R.id.et_password);
                MqttParameters parameters = mqttList.get(flexboxLayout.indexOfChild(v) - 2);
                etServerIp.setText(parameters.serverIp);
                etPort.setText(parameters.port);
                etClientId.setText(parameters.clientId);
                etUserName.setText(parameters.userName);
                etPassword.setText(parameters.passWord);
                final View view = v;
                builder.setPositiveButton("启用", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int viewIndex = flexboxLayout.indexOfChild(view) - 2;
                        mqttList.set(viewIndex, new MqttParameters(
                                etServerIp.getText().toString()
                                , etPort.getText().toString()
                                , etClientId.getText().toString()
                                , etUserName.getText().toString()
                                , etPassword.getText().toString()
                        ));
                        updateFlex(viewIndex);
                    }
                });
                builder.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int viewIndex = flexboxLayout.indexOfChild(view) - 2;
                        mqttList.remove(viewIndex);
                        deleteFlex(viewIndex);
                    }
                });
                builder.setView(dialogView).create().show();
            }
        });
        flexboxLayout.addView(view, params);
    }

    public void saveToFile() {
        BufferedWriter bufferedWriter = null;
        try {
            if (!mqttFile.exists())
                mqttFile.createNewFile();
            bufferedWriter = new BufferedWriter(new FileWriter(mqttFile));
            bufferedWriter.write(gson.toJson(mqttList));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadFromFile() {
        BufferedReader bufferedReader = null;
        try {
            if (!mqttFile.exists())
                mqttFile.createNewFile();
            else {
                bufferedReader = new BufferedReader(new FileReader(mqttFile));
                mqttList = gson.fromJson(bufferedReader.readLine(),
                        new TypeToken<ArrayList<MqttParameters>>() {
                        }.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mqttList == null) mqttList = new ArrayList<>();
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
