package org.grasshopper001.loomo_v1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.tts.TtsListener;

public class MainActivity extends BaseActivity {
    private int curFloor = 1;
    private int dstFloor;
    private String mqttServ = "";
    private static final int doorStat=0x002;
    private static final int refresh =0x003;
    private Button f1_bt;
    private Button f2_bt;
    private TextView main_text;
    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    Mqtt_lift mMqtt_lift;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        Intent intent = getIntent();
        mqttServ = intent.getStringExtra("mqttService");
        if(mqttServ!=null && mqttServ.equals("call lift")){
            curFloor = pref.getInt("curFloor",0);
            dstFloor = pref.getInt("dstFloor",0);
            f1_bt.setVisibility(View.GONE);
            f2_bt.setVisibility(View.GONE);
            mMqtt_lift = new Mqtt_lift();
            mMqtt_lift.setMqttHandler(mqttHandler);
            mMqtt_lift.call_lift(curFloor,dstFloor);
            main_text.setText("Success");
            speak("Success");
        }
    }

    private final Handler mqttHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what==refresh){
                f1_bt.setVisibility(View.VISIBLE);
                f2_bt.setVisibility(View.VISIBLE);
                main_text.setText("please choose");
                speak("arrived");
                mMqtt_lift.stop();
                editor.putInt("curFloor",dstFloor);
                editor.putInt("dstFloor",0);
                editor.commit();
                curFloor = dstFloor;
                dstFloor = 0;

            }
            if(msg.what==doorStat){
                String mDoor=msg.getData().getString("doorStat");
                int mFloor=msg.getData().getInt("floorStat");
                switch (mqttServ){
                    case "call lift":
                        if(mDoor.equals("opened") && mFloor==curFloor){
                            speak("door opened");
                            System.out.println("floor"+curFloor+" door opened");
                            mBase.cleanOriginalPoint();
                            mPose2d = mBase.getOdometryPose(-1);
                            mBase.setOriginalPoint(mPose2d);
                            mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                            mBase.setUltrasonicObstacleAvoidanceEnabled(true);
                            mBase.setUltrasonicObstacleAvoidanceDistance((float)0.4);
                            mBase.setObstacleStateChangeListener(new ObstacleStateChangedListener() {
                                @Override
                                public void onObstacleStateChanged(int ObstacleAppearance) {
                                    if(ObstacleAppearance >= 1)
                                        speak("Excuse me");
                                }
                            });
                            mBase.setLinearVelocity(5.0f);
                            mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                                @Override
                                public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                                    Log.d("take lift", "onCheckPointArrived: true");
                                     mqttServ = "prepare to go out";

                                }
                                @Override
                                public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

                                }
                        });
                            mBase.addCheckPoint(1.6f, 0,0);
                            mHead.setWorldYaw(0);
                        }
                        break;

                    case "prepare to go out":
                        if(mDoor.equals("opening") && mFloor==dstFloor){
                            mBase.cleanOriginalPoint();
                            mPose2d = mBase.getOdometryPose(-1);
                            mBase.setOriginalPoint(mPose2d);
                            mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                            mBase.setLinearVelocity(5.0f);
                            mBase.setUltrasonicObstacleAvoidanceEnabled(true);
                            mBase.setUltrasonicObstacleAvoidanceDistance((float)0.4);
                            mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                                @Override
                                public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                                    Log.d("go out", "onCheckPointArrived: true");
                                    System.out.println("check point arrived");
                                    Message msg = new Message();
                                    msg.what = refresh;
                                    mqttHandler.sendMessage(msg);
                                }

                                @Override
                                public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                                    System.out.println("check point missed");
                                    Log.d("go out", "conCheckPointMiss:true");
                                    mBase.addCheckPoint(0,0,0);

                                }
                            });
                            mBase.addCheckPoint(-1.6f,0,theta);
                        }
                    default:
                        break;
                }
            }
        }
    };

    public void speak(String s){
        try{
            mSpeaker.speak(s, new TtsListener() {
                @Override
                public void onSpeechStarted(String word) {

                }

                @Override
                public void onSpeechFinished(String word) {

                }

                @Override
                public void onSpeechError(String word, String reason) {

                }
            });
        }catch (VoiceException e){
            Log.e("speaker","error msg:"+e.getMessage());
        }
    }
    /* qrBut
     * go to qr activity
     * scan QR code
     */
    public void qrBut(View view){
        Intent qrAct=new Intent(this,qr.class);
        startActivity(qrAct);
    }

    private void init(){
    pref = getSharedPreferences("floor", Context.MODE_PRIVATE);
    editor = pref.edit();
    f1_bt = (Button)findViewById(R.id.f1_button);
    f2_bt = (Button)findViewById(R.id.f2_button);
    main_text = (TextView)findViewById(R.id.main_text);
    f1_bt.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(curFloor == 2) {
                editor.putInt("curFloor", 2);
                editor.putInt("dstFloor", 1);
                editor.commit();
                qrBut(v);
            }else{
                main_text.setText("Already here");
            }
        }
    });
    f2_bt.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(curFloor == 1) {
                editor.putInt("curFloor", 1);
                editor.putInt("dstFloor", 2);
                editor.commit();
                qrBut(v);
            }else main_text.setText("Already here");
        }
    });
    }

}
