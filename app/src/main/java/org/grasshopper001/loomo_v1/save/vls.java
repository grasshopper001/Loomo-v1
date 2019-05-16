package org.grasshopper001.loomo_v1.save;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;

import org.grasshopper001.loomo_v1.BaseActivity;
import org.grasshopper001.loomo_v1.MainActivity;
import org.grasshopper001.loomo_v1.R;


public class vls extends BaseActivity {
    private static final String TAG = "RobotControl";
    private float theta = (float)Math.PI;
    private String vlsMode;
    private TextView vlsInfo;
    private static final int headStat=0x003;
    private static final int baseStat=0x004;

    private Handler vlsHandler = new Handler(){
        public boolean headCon = false;
        public boolean baseCon = false;
        @Override
        public void handleMessage(Message msg){
            if(msg.what==headStat) headCon=true;
            if(msg.what==baseStat) baseCon=true;
            if(headCon && baseCon){
                switch (vlsMode){
                    case "take lift":
                        mBase.cleanOriginalPoint();
                        mPose2d = mBase.getOdometryPose(-1);
                        mBase.setOriginalPoint(mPose2d);
                        mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);
                        mBase.setUltrasonicObstacleAvoidanceEnabled(true);
                        mBase.setUltrasonicObstacleAvoidanceDistance((float)0.4);
                        mBase.setLinearVelocity(5.0f);
                        mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                            @Override
                            public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                                Log.d(TAG, "onCheckPointArrived: true");
                                System.out.println("check point arrived");
                                /* call mqtt service
                                 * check floor info and get out of the lift
                                 */
                                Intent vlsGot = getIntent();
                                Intent inLift = new Intent(vls.this, mqtt.class);
                                inLift.putExtra("mqttService","prepare to go out");
                                inLift.putExtra("Fstart",vlsGot.getIntExtra("Fstart",1));
                                inLift.putExtra("Fend",vlsGot.getIntExtra("Fend",2));
                                startActivity(inLift);

                            }

                            @Override
                            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                                System.out.println("check point missed");
                                Log.d(TAG, "conCheckPointMiss:true");
                                mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                                    @Override
                                    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                                        //sleep(14000);
                                        Intent vlsGot=getIntent();
                                        Intent misLift=new Intent(vls.this,mqtt.class);
                                        misLift.putExtra("mqttService","wait(5000);call lift");
                                        misLift.putExtra("Fstart",vlsGot.getIntExtra("Fstart",1));
                                        misLift.putExtra("Fend",vlsGot.getIntExtra("Fend",2));
                                        startActivity(misLift);
                                    }

                                    @Override
                                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

                                    }
                                });
                                mBase.addCheckPoint(0,0,0);
                            }
                        });
                        mBase.addCheckPoint(1.6f, 0,0);
                        mHead.setWorldYaw(0);
                        break;
                    case "go ahead":
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
                                Log.d(TAG, "onCheckPointArrived: true");
                                System.out.println("check point arrived");
                                /* call mqtt service
                                 * check floor info and get out of the lift
                                 */
                                Intent home = new Intent(vls.this, MainActivity.class);
                                startActivity(home);

                            }

                            @Override
                            public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {
                                System.out.println("check point missed");
                                Log.d(TAG, "conCheckPointMiss:true");
                                mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                                    @Override
                                    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                                        Intent vlsGot=getIntent();
                                        Intent misOut=new Intent(vls.this,mqtt.class);
                                        misOut.putExtra("mqttService","prepare to go out");
                                        misOut.putExtra("Fstart",vlsGot.getIntExtra("Fstart",1));
                                        misOut.putExtra("Fend",vlsGot.getIntExtra("Fend",2));
                                        startActivity(misOut);
                                    }

                                    @Override
                                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

                                    }
                                });
                                mBase.addCheckPoint(0,0,0);

                            }
                        });
                        mBase.addCheckPoint(-1.6f,0,theta);
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vls);
        vlsInfo=findViewById(R.id.vlsInfo);
        //instant
        Intent vls=getIntent();
        vlsMode=vls.getStringExtra("vls mode");
        //moving mode
        vlsInfo.setText("vls mode: "+vlsMode);
        init();


    }
    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }
    private void init(){
        mHead = Head.getInstance();
        mHead.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "onBind: Head");
                System.out.println("head binded");
                Message headBinded = new Message();
                headBinded.what=headStat;
                vlsHandler.sendMessage(headBinded);
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "onUnbind: Head");
                System.out.println("head unbinded");
            }
        });
        mBase = Base.getInstance();
        mBase.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "onBind: base");
                System.out.println("base binded");
                Message baseBinded = new Message();
                baseBinded.what=baseStat;
                vlsHandler.sendMessage(baseBinded);
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "onUnbind: base");
                System.out.println("base unbinded");
            }
        });
    }
}
