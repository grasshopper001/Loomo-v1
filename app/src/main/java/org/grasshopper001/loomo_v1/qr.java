package org.grasshopper001.loomo_v1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamType;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import static android.os.SystemClock.sleep;

public class qr extends AppCompatActivity {
    private TextView qrInfo;
    private ImageView qrScanner;
    private Vision mVision;
    private Bitmap mBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
    private static final int qrImg=0x001;

    static public Bitmap rgb2Bitmap(byte[] data, int width, int height) {
        int[] colors = convertByteToColor(data);    //取RGB值转换为int数组
        if (colors == null) {
            return null;
        }

        Bitmap bmp = Bitmap.createBitmap(colors, 0, width, width, height,
                Bitmap.Config.ARGB_8888);
        return bmp;
    }

    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    public static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }


    // 将纯RGB数据数组转化成int像素数组
    public static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size / 3 + arg];
        int red, green, blue;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    public static byte[] bitmap2RGB(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();  //返回可用于储存此位图像素的最小字节数

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //  使用allocate()静态方法创建字节缓冲区
        bitmap.copyPixelsToBuffer(buffer); // 将位图的像素复制到指定的缓冲区

        byte[] rgba = buffer.array();byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;

        //Bitmap像素点的色彩通道排列顺序是RGBA
        for (int i = 0; i < count; i++) {

            pixels[i * 3] = rgba[i * 4];        //R
            pixels[i * 3 + 1] = rgba[i * 4 + 1];    //G
            pixels[i * 3 + 2] = rgba[i * 4 + 2];       //B

        }

        return pixels;
    }

    private final Handler qrHandler =new Handler(){
        public int period=50;
        @Override
        public void handleMessage(Message msg){
            if(msg.what==qrImg){
                Bitmap toShow=msg.getData().getParcelable("bitmap");
                qrScanner.setImageBitmap(toShow);
                period--;
                if(period==0){
                    period=50;
                    QRCodeReader reader=new QRCodeReader();
                    try{
                        Hashtable<DecodeHintType,String> hints = new Hashtable<>();
                        hints.put(DecodeHintType.CHARACTER_SET,"UTF8");
                        RGBLuminanceSource source=new RGBLuminanceSource(640,480,convertByteToColor(bitmap2RGB(toShow)));
                        BinaryBitmap toScan = new BinaryBitmap(new HybridBinarizer(source));
                        Result result=reader.decode(toScan,hints);
                        qrScanner.setImageBitmap(toShow);
                        qrInfo.setText("got QR code:"+result.toString());
                        /* get the result and go to mqtt activity
                         *
                         */
                        Intent qrGot=getIntent();
                        Intent qrScanned=new Intent(qr.this,mqtt.class);
                        qrScanned.putExtra("mqttService","call lift");
                        qrScanned.putExtra("Fstart",qrGot.getIntExtra("Fstart",1));
                        qrScanned.putExtra("Fend",qrGot.getIntExtra("Fend",2));
                        startActivity(qrScanned);
                    }catch(NotFoundException e){
                        e.printStackTrace();
                        qrInfo.setText("NOT FOUND QR CODE");
                    }catch(ChecksumException e){
                        e.printStackTrace();
                    }catch(FormatException e){
                        e.printStackTrace();
                    }
                }

            }
        }


    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        qrInfo=findViewById(R.id.qrInfo);
        qrScanner=findViewById(R.id.qrScanner);
        // get Vision SDK instance
        mVision = Vision.getInstance();
        mVision.bindService(this, mBindStateListener);
    }

    ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            qrInfo.setText("service binded\n");
            //get color image
            mVision.startListenFrame(StreamType.COLOR, new Vision.FrameListener() {
                @Override
                public void onNewFrame(int streamType, Frame frame) {
                    System.out.println("new frame come in");
                    mBitmap.copyPixelsFromBuffer(frame.getByteBuffer());
                    Message msg=new Message();
                    msg.what=qrImg;
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("bitmap",mBitmap);
                    msg.setData(bundle);
                    qrHandler.sendMessage(msg);
                    System.out.println("photo updated");
                }
            });
        }

        @Override
        public void onUnbind(String reason) {
            System.out.println("system not conected");
            qrInfo.setText("failed at binding service."+reason+". the connect shut down.");
        }
    };
    @Override
    protected void onStop(){
        super.onStop();
        mVision.stopListenFrame(StreamType.COLOR);
        mVision.unbindService();
        finish();
    }
}
