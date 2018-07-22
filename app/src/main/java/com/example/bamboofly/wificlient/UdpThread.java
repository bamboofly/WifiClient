package com.example.bamboofly.wificlient;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class UdpThread extends Thread {

    private final int mPort;
    private MediaCodec mMediaCodec;
    private boolean initMediaCodecFlags;

    public UdpThread(MediaCodec mediaCodec,int port){
        mMediaCodec = mediaCodec;
        mPort = port;
    }
    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    @Override
    public void run() {
        Log.e("lianghuan","udpthread run");
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket(mPort);
            DatagramPacket datagramPacket = new DatagramPacket(new byte[64 * 1024],64 * 1024);
            int count = 0;
            while (count < 500){
                datagramSocket.receive(datagramPacket);
                int length = datagramPacket.getLength();
                byte[] data = datagramPacket.getData();
                Log.e("lianghuan","length = "+length);

                int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0){
                    ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);



                    inputBuffer.put(data,0,length);
                    inputBuffer.rewind();

                    mMediaCodec.queueInputBuffer(inputBufferIndex,0,length,0,0);

                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(info, 100000);
                    Log.e("lianghuan","outputBufferIndex = "+outputBufferIndex);
                    Log.e("lianghuan","info.size = "+info.size);
                    if (outputBufferIndex >= 0){
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex,info.size > 0);
                    }
                }
                count++;
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
