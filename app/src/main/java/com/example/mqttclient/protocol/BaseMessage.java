package com.example.mqttclient.protocol;

public class BaseMessage {
    public enum Type {
        INT(1),
        FLOAT(2),
        BOOL(3),
        AIR_CONDITIONING(4);
        int index;

        Type(int index) {
            this.index = index;
        }
    }
    int type;
}
