package com.example.adeem.tankgame;



import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class multiplayer_SigninPage extends AppCompatActivity implements  WifiP2pManager.ChannelListener{

    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    WiFiDirectReceiver wfdReceiver;

    private Button hostBTN;
    private Button joinBTN;
    private TextView log;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_main_page);


        //bar.setVisibility(View.VISIBLE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), this);

        this.hostBTN = (Button) findViewById(R.id.host_button);
        this.joinBTN = (Button) findViewById(R.id.join_button);
        this.bar = (ProgressBar) findViewById(R.id.pbar_multiMAIN);
        this.log =(TextView) findViewById(R.id.log_txt);
        bar.setVisibility(View.VISIBLE);

        registerWfdReceiver();
        wfdReceiver.setBarAndText(this.bar,this.log,joinBTN);

        this.hostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
                onDiscover();
            }
        });
        this.joinBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.setText(log.getText() +"\n calling on connect");//check patin
                onConnect();
            }
        });
    }

    public void onChannelDisconnected(){
        log.append("\nWiFi Direct Disconnected - Reinitialize.");
        reinitializeChannel();
    }

    private void reinitializeChannel(){
        mChannel = mManager.initialize(this, getMainLooper(), this);
        log.append("\nWiFi Direct Channel Initialization: " + ((mChannel != null)? "SUCCESS" : "FAILED"));
    }

    private void registerWfdReceiver(){
        wfdReceiver = new WiFiDirectReceiver(mManager, mChannel, this);
        wfdReceiver.registerReceiver();
        this.bar.setVisibility(View.GONE);
    }

    private void unRegisterWfdReceiver(){
        if(wfdReceiver != null)
            wfdReceiver.unregisterReceiver();
        wfdReceiver = null;
    }

    @Override
    public void onBackPressed() {
        unRegisterWfdReceiver();
        super.onBackPressed();
    }

    public void onDiscover(){
        if(isWfdReceiverRegisteredAndFeatureEnabled()){
            mManager.discoverPeers(mChannel,
                    new ActionListenerHandler(this, "Discover Peers"));
        }
    }

    public void onConnect(){
        if(isWfdReceiverRegisteredAndFeatureEnabled()){
            WifiP2pDevice theDevice = wfdReceiver.getFirstAvailableDevice();
            if(theDevice != null){
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = theDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                mManager.connect(mChannel, config, new ActionListenerHandler(this, "Connection"));
            }
        }
    }

    private boolean isWfdReceiverRegisteredAndFeatureEnabled(){
        return (wfdReceiver != null && wfdReceiver.isWifiDirectEnabled);
    }
}