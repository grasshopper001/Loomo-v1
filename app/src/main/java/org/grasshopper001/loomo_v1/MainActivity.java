package org.grasshopper001.loomo_v1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private int Fstart = 1;
    private int Fend = 2;
    private EditText mCurFloor;
    private EditText mDstFloor;
    private TextView mSetFloorInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurFloor =findViewById(R.id.curFloor);
        mDstFloor =findViewById(R.id.dstFloor);
        mSetFloorInfo = findViewById(R.id.setFloorInfo);
        Intent maGot=getIntent();
        mSetFloorInfo.setText(maGot.getStringExtra("errInfo"));
    }
    /* enter
     * enters floor info
     * if not valid input, rebind to MainActivity
     */
    public void enter(View view){
        try{
            Fstart = Integer.parseInt(mCurFloor.getText().toString());
            Fend = Integer.parseInt(mDstFloor.getText().toString());
            mSetFloorInfo.setText("set floor info success");
        }catch(NumberFormatException e){
            Intent misInput = new Intent(MainActivity.this,MainActivity.class);
            misInput.putExtra("errInfo","not valid input floors");
            startActivity(misInput);
        }
    }
    /* mqttBut
     * go to mqtt activity
     * call the lift
     */
    public void mqttBut(View view){
        Intent mqttAct=new Intent(this,mqtt.class);
        mqttAct.putExtra("mqttService","call lift");
        mqttAct.putExtra("Fstart",Fstart);
        mqttAct.putExtra("Fend",Fend);
        startActivity(mqttAct);
    }
    /* vlsBut
     * go to vls activity
     * go ahead
     */
    public void vlsBut(View view){
        Intent vlsAct=new Intent(this, vls.class);
        vlsAct.putExtra("vls mode","go ahead");
        startActivity(vlsAct);
    }
    /* qrBut
     * go to qr activity
     * scan QR code
     */
    public void qrBut(View view){
        Intent qrAct=new Intent(this,qr.class);
        qrAct.putExtra("Fstart",Fstart);
        qrAct.putExtra("Fend",Fend);
        startActivity(qrAct);
    }
}
