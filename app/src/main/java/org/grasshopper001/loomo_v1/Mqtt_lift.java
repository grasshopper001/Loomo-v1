package org.grasshopper001.loomo_v1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt_lift {
    private String username="admin";
    private String password="admin";
    private String broker="tcp://47.96.26.134:1883";
    private String clientId="loomo";
    private static final int doorStat=0x002;
    private MqttClient client;
    private String mqttServ;
    private Handler mqttHandler;
    public void setMqttHandler(Handler handler){
        mqttHandler = handler;
    }
    public void call_lift(int curFloor,int dstFloor){

        /* publish a call lift message
         * if success: call lift msg published
         * if fail: failed at + loc. reasoncode + msg +cause
         */
        Log.e("in call lift",":true");
        String Tcall = "port/rnd/call";
        String content_1to2 = "001141|0557673|";
        String content_2to1 = "001142|0557673|";
        final String Tdoor = "port/rnd/door";
        String Tlift = "port/rnd/travel";
        int qos = 1;
        MemoryPersistence persistence = new MemoryPersistence();
        try{
            client=new MqttClient(broker,clientId,persistence);
            MqttConnectOptions conOpts=new MqttConnectOptions();
            conOpts.setUserName(username);
            conOpts.setPassword(password.toCharArray());
            client.connect(conOpts);
            MqttMessage msg;
            Log.e("floor","cur =" + curFloor+",dst =" + dstFloor);
            if(curFloor==1 && dstFloor ==2){
                msg =new MqttMessage(content_1to2.getBytes());
                msg.setQos(qos);
                client.publish(Tcall,msg);
            }
            if(curFloor==2 && dstFloor==1){
                msg = new MqttMessage(content_2to1.getBytes());
                msg.setQos(qos);
                client.publish(Tcall,msg);
            }

            client.subscribe(Tdoor);
            client.subscribe(Tlift);
            MqttCallback mqttCallback=new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload=new String(message.getPayload());
                    System.out.println(topic);
                    System.out.println(payload);
                    if(topic.equals(Tdoor)){
                        //analyzing payload
                        JSONObject objDoor= JSON.parseObject(payload);
                        String doorStatus = objDoor.getString("door");
                        int floorStatus = objDoor.getIntValue("floor");
                        System.out.println("received Tdoor: "+doorStatus);
                        Message msg1=new Message();
                        msg1.what=doorStat;
                        Bundle bundle=new Bundle();
                        bundle.putString("doorStat",doorStatus);
                        bundle.putInt("floorStat",floorStatus);
                        msg1.setData(bundle);
                        mqttHandler.sendMessage(msg1);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            };
            client.setCallback(mqttCallback);

        }catch (MqttException e){
           System.out.println("failed at"+e.getLocalizedMessage()+"."+e.getReasonCode()+e.getMessage()+e.getCause());
        }
    }
    public void stop(){
        try{
            client.disconnect();
            client.close();
            System.out.println("mqtt service stopped");
        }catch(MqttException e){
            e.printStackTrace();
        }
    }
    }

