package com.example.mqttclient.mqtt;

public class MqttParameters {
    public String serverIp;
    public String port;
    public String clientId;
    public String userName;
    public String passWord;

    public MqttParameters(String serverIp, String port, String clientId, String userName, String passWord) {
        this.serverIp = serverIp;
        this.port = port;
        this.clientId = clientId;
        this.userName = userName;
        this.passWord = passWord;
    }

    public String getUri(){
        return "tcp://"+serverIp+":"+port;
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
