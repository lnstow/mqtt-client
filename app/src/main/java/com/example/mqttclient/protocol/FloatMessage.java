package com.example.mqttclient.protocol;

public class FloatMessage extends BaseMessage {
    public float value;

    public FloatMessage(float value) {
        this.value = value;
        this.type = Type.FLOAT.index;
    }
}
