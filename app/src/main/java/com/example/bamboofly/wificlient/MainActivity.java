package com.example.bamboofly.wificlient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.example.bamboofly.wificlient.wifitools.WifiMgr;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiMgr mWifiMgr;
    private ApClientThread mApClient;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.usbpreview_surfaceview);
        mHolder = mSurfaceView.getHolder();


        registerWifiReceiver();
        mWifiMgr = new WifiMgr(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        boolean disconnectWifi = mWifiMgr.disconnectWifi("lianghuan");
//        Log.e("lianghuan","disconnectWifi = "+disconnectWifi);
        if (mApClient != null){
            mApClient.stopClient();
        }
    }

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        //监听WiFi开启与关闭
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        //监听扫描周围可用WiFi列表结果
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        //监听WiFi连接与断开
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        //注册广播
        registerReceiver(mWifiBroadcaseReceiver, filter);
    }

    private BroadcastReceiver mWifiBroadcaseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("lianghuan","action = "+action);
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                final List<ScanResult> scanResults = mWifiMgr.getScanResults();
                for (final ScanResult result :
                        scanResults) {
                    Log.e("lianghuan", "result = " + result.SSID);
                    if (result.SSID.equals("lianghuan")){
                        unregisterReceiver(this);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mWifiMgr.connectWifi(result.SSID,"lianghuan",scanResults);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    Log.e("lianghuan","e = "+e.getLocalizedMessage());
                                }
                            }
                        },5000);
                    }
                }
            }

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info != null){
                    if (info.getState().equals(NetworkInfo.State.CONNECTED)){
                        String connectedSSID = mWifiMgr.getConnectedSSID();
                        Log.d("lianghuan","connect "+connectedSSID+" success");
                        String ipAddressFromHotspot = mWifiMgr.getIpAddressFromHotspot();

                        mApClient = new ApClientThread(mHolder.getSurface(),ipAddressFromHotspot,12345,mWifiMgr);
                        mApClient.start();
                        Log.e("lianghuan","ipAddressFromHostspot = "+ipAddressFromHotspot);
                    }else {
                        String ipAddressFromHotspot = mWifiMgr.getIpAddressFromHotspot();
                        Log.e("lianghuan","ipAddressFromHostspot = "+ipAddressFromHotspot);
                        Log.e("lianghuan","no connect");
                    }
                }
            }
//            mWifiMgr.startScan();

        }
    };

    public void scanAndConnect(View view) {
        mWifiMgr.openWifi();
        boolean res = mWifiMgr.startScan();
        Log.e("lianghuan","res = "+res);
    }

    private Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };
}
