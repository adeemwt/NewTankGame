package com.example.adeem.tankgame;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.adeem.tankgame.multiplayer_mainPage;

/**
 * Created by adeem on 02/11/2016.
 */

public class WiFiDirectReceiver   extends BroadcastReceiver implements
        WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener
{
    public static final int PORT = 7890;

    Intent intent;

    boolean isWifiDirectEnabled;
    WifiP2pManager wfdManager;
    WifiP2pManager.Channel wfdChannel;
    multiplayer_mainPage appActivity;
    private IntentFilter intentFilter;
    WifiP2pDevice[] wfdDevices;

    ProgressBar bar;
    public WiFiDirectReceiver(WifiP2pManager wfdManager, WifiP2pManager.Channel wfdChannel, multiplayer_mainPage appActivity){
        this.wfdManager = wfdManager;
        this.wfdChannel = wfdChannel;
        this.appActivity = appActivity;
    }

public void  setBar(ProgressBar bar){
    this.bar = bar;
}

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Toast.makeText(appActivity.getApplicationContext(), action, Toast.LENGTH_SHORT).show();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            handleWifiP2pStateChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            handleWifiP2pPeersChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            handleWifiP2pConnectionChanged(intent);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            handleWifiP2pThisDeviceChanged(intent);
        }
    }

    private void handleWifiP2pStateChanged(Intent intent){
        int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
        isWifiDirectEnabled = state == WifiP2pManager.WIFI_P2P_STATE_ENABLED ? true : false;
        Toast.makeText(appActivity, "Enabled : " +isWifiDirectEnabled, Toast.LENGTH_SHORT).show();
    }

    private void handleWifiP2pThisDeviceChanged(Intent intent){
        WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        this.bar.setVisibility(View.GONE);
    }

    private void handleWifiP2pPeersChanged(Intent intent){
        // The list of available peers has changed
        //Request the current list of peers

        wfdManager.requestPeers(wfdChannel, this);
        this.bar.setVisibility(View.GONE);
    }

    private void handleWifiP2pConnectionChanged(Intent intent){
        // A connection is in place
        NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        if(info != null && info.isConnected()){
            wfdManager.requestConnectionInfo(wfdChannel, this);
        }
        else{
            Toast.makeText(appActivity.getApplicationContext(), "Connection Closed", Toast.LENGTH_SHORT).show();
        }
    }

    public void registerReceiver(){
        appActivity.registerReceiver(this, getIntentFilter());
    }

    public void unregisterReceiver(){
        appActivity.unregisterReceiver(this);
    }

    private IntentFilter getIntentFilter(){
        if(intentFilter == null){
            intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        }
        return intentFilter;
    }
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        if(wifiP2pInfo.groupFormed){
            if(wifiP2pInfo.isGroupOwner)//start server
                intent = new Intent(appActivity, server_inGame.class);//start game!!! (multi ingame)
            else//start client
                intent = new Intent(appActivity, clientInGame.class);//start game!!! (multi ingame)

            intent.putExtra("WIFI_P2P_INFO", wifiP2pInfo);
            appActivity.startActivity(intent);

        }
    }


    public void startGame(){
        appActivity.startActivity(intent);
    }
    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        if(wifiP2pDeviceList != null &&
                wifiP2pDeviceList.getDeviceList() != null &&
                wifiP2pDeviceList.getDeviceList().size() > 0){
            wfdDevices = wifiP2pDeviceList.getDeviceList().toArray(new WifiP2pDevice[0]);
        }
        else {
            Toast.makeText(appActivity.getApplicationContext(), wifiP2pDeviceList.toString(), Toast.LENGTH_SHORT).show();
            wfdDevices = null;
        }
    }

    public WifiP2pDevice getFirstAvailableDevice(){
        if(wfdDevices != null) {
            return wfdDevices[0];
        }
        return null;
    }
}
