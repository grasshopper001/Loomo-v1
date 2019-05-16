package org.grasshopper001.loomo_v1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.voice.Languages;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;

public class BaseActivity extends AppCompatActivity {
    public static Base mBase;
    public static Pose2D mPose2d;
    public static Head mHead;
    public static float theta = (float)Math.PI;
    public static Speaker mSpeaker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mHead == null) {
            mHead = Head.getInstance();
            mHead.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
                @Override
                public void onBind() {

                }

                @Override
                public void onUnbind(String reason) {

                }
            });
        }
        if (mBase == null) {
            mBase = Base.getInstance();
            mBase.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
                @Override
                public void onBind() {

                }

                @Override
                public void onUnbind(String reason) {

                }
            });
        }
        if(mSpeaker == null){
            mSpeaker = Speaker.getInstance();
            mSpeaker.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
                @Override
                public void onBind() {

                }

                @Override
                public void onUnbind(String reason) {

                }
            });
            try{
                mSpeaker.setVolume(100);
            }catch (VoiceException e){
                System.out.println(e.getMessage());
            }
        }

    }

    @Override
    protected void onStop(){
        super.onStop();
    }

}
