package com.example.mqttclient;

public class MqttParameters {
    public String uri;
    public String clientId;
    public String userName;
    public String passWord;

    public MqttParameters(String uri, String clientId, String userName, String passWord) {
        this.uri = uri;
        this.clientId = clientId;
        this.userName = userName;
        this.passWord = passWord;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getUri() {
        return uri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassWord() {
        return passWord;
    }
}
