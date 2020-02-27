package com.example.mqttclient.protocol;

public class AirConditioningMessage extends BaseMessage {
    public float value;
    public boolean state;
    public AirConditioningMessage(boolean state, float value){
        this.state = state;
        this.value = value;
        this.type = Type.AIR_CONDITIONING.index;
    }
}
