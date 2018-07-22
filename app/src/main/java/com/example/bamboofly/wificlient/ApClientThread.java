package com.example.bamboofly.wificlient;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import com.example.bamboofly.wificlient.wifitools.WifiMgr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ApClientThread extends Thread {

    private final String mIp;
    private final int mPort;
    private final WifiMgr mWifiMgr;
    private final Surface mSurface;
    private Socket mSocket;
    private boolean mRunFlag;
    private MediaCodec mMediaCodec;

    public ApClientThread(Surface surface,String ip, int port, WifiMgr wifiMgr){
        mSurface = surface;
        mIp = ip;
        mPort = port;
        mWifiMgr = wifiMgr;
    }

    @Override
    public void run() {
        Log.e("lianghuan","client run");
        try {
            mSocket = new Socket(mIp,mPort);

            InputStream inputStream = mSocket.getInputStream();
            OutputStream outputStream = mSocket.getOutputStream();

            int read = inputStream.read();
            if (read == 1){

                String hostAddress = mWifiMgr.getIpAddress();
                Log.e("lianghuan","hostAddress = "+hostAddress);
//                String hostIP = WifiMgr.getHostIP();
//                Log.e("lianghuan","hostIp = "+hostIP);
                outputStream.write(hostAddress.getBytes());
            }
            int read1 = inputStream.read();
            if (read1 == 2){
                outputStream.write("12346".getBytes());
            }

            byte[] extraData = new byte[100];
            int extraData_size = inputStream.read(extraData);
            initDecodec(extraData);

            Log.e("lianghuan","extraData = "+toHexString(extraData));
            outputStream.write(1);

            new UdpThread(mMediaCodec,12346).start();
            int heart = inputStream.read();
            Log.e("lianghuan","heart = "+heart);
            while (heart == 0x15){
                Log.e("lianghuan","heart jump");
                outputStream.write(1);
                heart = inputStream.read();

            }

//            DatagramSocket datagramSocket = new DatagramSocket(12346);
//            DatagramPacket datagramPacket = new DatagramPacket(new byte[1024],1024);
//            int count = 0;
//            while (count < 100){
//                datagramSocket.receive(datagramPacket);
//                int length = datagramPacket.getLength();
//                Log.e("lianghuan","length = "+length);
//                count++;
//            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("lianghuan","e = "+e.getMessage());
        }




        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() {
        mRunFlag = true;
        super.start();
    }

    public void stopClient(){
        mRunFlag = false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initDecodec(byte[] extraData){
        Log.e("lianghuan","-------------------");
        try {
            MediaCodec decoderByType = MediaCodec.createDecoderByType("video/avc");
            MediaFormat videoFormat = MediaFormat.createVideoFormat("video/avc", 640, 480);


            byte[] head = new byte[]{0,0,0,1};

            int sps_len_index = 7;
            int sps_len = extraData[7];
            Log.e("lianghuan","sps_len = "+sps_len);
            byte[] sps = new byte[sps_len + 4];

            System.arraycopy(head,0,sps,0,4);
            System.arraycopy(extraData,sps_len_index + 1,sps,4,sps_len);
            int pps_len_index = 7 + sps_len + 3;
            int pps_len = extraData[pps_len_index];
            Log.e("lianghuan","pps_len = "+pps_len);
            byte[] pps = new byte[pps_len + 4];

            System.arraycopy(head,0,pps,0,4);
            System.arraycopy(extraData,pps_len_index + 1,pps,4,pps_len);

            videoFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
            videoFormat.setByteBuffer("csd-1",ByteBuffer.wrap(pps));
//            Log.e("lianghuan","sps = "+bytesToHexString(sps));
//            Log.e("lianghuan","pps = "+bytesToHexString(pps));

            //videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE,25);

            decoderByType.configure(videoFormat,mSurface,null,0);

            mMediaCodec = decoderByType;

            mMediaCodec.start();

//            int inputBufferIndex = mMediaCodec.dequeueInputBuffer(100000);
//            if (inputBufferIndex >= 0){
//                Log.e("lianghuan","inputBuffer ----- 1 = "+inputBufferIndex);
//                ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
//                inputBuffer.clear();
//                inputBuffer.put(ByteBuffer.wrap(sps));
//                mMediaCodec.queueInputBuffer(inputBufferIndex,0,sps.length,0,MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
//
//                int inputBuffer1 = mMediaCodec.dequeueInputBuffer(100000);
//                if (inputBuffer1 >= 0){
//                    Log.e("lianghuan","inputBuffer ------2 = "+inputBuffer1);
//                    ByteBuffer inputBuffer2 = mMediaCodec.getInputBuffer(inputBuffer1);
//                    inputBuffer2.clear();
//                    inputBuffer2.put(ByteBuffer.wrap(pps));
//                    mMediaCodec.queueInputBuffer(inputBuffer1,0,pps.length,0,MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
//                }
//            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("lianghuan","e = "+e.getMessage());
        }
    }

    public static String toHexString(byte[] byteArray) {
        final StringBuilder hexString = new StringBuilder("");
        if (byteArray == null || byteArray.length <= 0)
            return null;
        for (int i = 0; i < byteArray.length; i++) {
            int v = byteArray[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                hexString.append(0);
            }
            hexString.append(hv);
        }
        return hexString.toString().toLowerCase();
    }
}
