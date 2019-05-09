package org.grasshopper001.loomo_v1;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class mqtt extends AppCompatActivity {
    /* mqtt settings
     * graped out from onCreate for a probable other method
     * eg: showing the status of lift by subscribing mqtt message
     */
    private String username="admin";
    private String password="admin";
    private String broker="tcp://47.96.26.134:1883";
    private String clientId="loomo";
    private static final int doorStat=0x002;
    private TextView mqttPub;
    private TextView mqttRec;
    private MqttClient client;
    private String mqttServ;
    private int dstFloor=2;
    private int curFloor=1;

    private final Handler mqttHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==doorStat){
                String mDoor=msg.getData().getString("doorStat");
                int mFloor=msg.getData().getInt("floorStat");
                mqttRec.setText("door status: "+mDoor+"\n"+"floor status: "+mFloor);
                switch (mqttServ){
                    case "call lift":
                        if(mDoor.equals("opened") && mFloor==curFloor){
                            try {
                                client.disconnect();
                                client.close();
                            }catch(MqttException e){
                                e.printStackTrace();
                            }
                            Intent takeLift=new Intent(mqtt.this,vls.class);
                            takeLift.putExtra("vls mode","take lift");
                            startActivity(takeLift);
                        }
                        break;
                    case "prepare to go out":
                        if(mDoor.equals("opened") && mFloor==dstFloor){
                            try {
                                client.disconnect();
                                client.close();
                            }catch(MqttException e){
                                e.printStackTrace();
                            }
                            Intent goOut=new Intent(mqtt.this,vls.class);
                            goOut.putExtra("vls mode","go ahead");
                            startActivity(goOut);
                        }
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        Intent mqttIntent=getIntent();
        mqttServ=mqttIntent.getStringExtra("mqttService");
        mqttPub=findViewById(R.id.mqttPub);
        mqttRec=findViewById(R.id.mqttRec);

        /* publish a call lift message
         * if success: call lift msg published
         * if fail: failed at + loc. reasoncode + msg +cause
         */
        String Tcall = "port/rnd/call";
        String content = "001141|0557673|";
        final String Tdoor = "port/rnd/door";
        String Tlift = "port/rnd/travel";
        int qos = 2;
        MemoryPersistence persistence = new MemoryPersistence();
        try{
            client=new MqttClient(broker,clientId,persistence);
            MqttConnectOptions conOpts=new MqttConnectOptions();
            conOpts.setUserName(username);
            conOpts.setPassword(password.toCharArray());
            client.connect(conOpts);
            MqttMessage msg=new MqttMessage(content.getBytes());
            msg.setQos(qos);

            client.publish(Tcall,msg);
            mqttPub.setText("call lift msg published");
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
            mqttPub.setText("failed at"+e.getLocalizedMessage()+"."+e.getReasonCode()+e.getMessage()+e.getCause());
        }


    }
}
