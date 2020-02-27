package com.example.mqttclient.protocol;

public class BoolMessage extends BaseMessage {
    public boolean value;
    public BoolMessage(boolean value){
        this.value = value;
        this.type = Type.BOOL.index;
    }
}
