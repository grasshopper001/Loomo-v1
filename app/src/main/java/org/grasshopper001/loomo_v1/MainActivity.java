package org.grasshopper001.loomo_v1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    /* mqttBut
     * go to mqtt activity
     * call the lift
     */
    public void mqttBut(View view){
        Intent mqttAct=new Intent(this,mqtt.class);
        mqttAct.putExtra("mqttService","call lift");
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
        startActivity(qrAct);
    }
}
