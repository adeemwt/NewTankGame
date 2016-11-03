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
import android.widget.Toast;


import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class multiplayer_mainPage extends AppCompatActivity implements  WifiP2pManager.ChannelListener{

    WifiP2pManager.Channel mChannel;
    WifiP2pManager mManager;
    WiFiDirectReceiver wfdReceiver;

    private Button hostBTN;
    private Button joinBTN;
    private Button connectBTN;
    private Button disconnectBTN;
    private TextView serverIP;
    private TextView log;
    private EditText serverIP_cliente;
    private Button startGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_main_page);



        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), this);

        this.hostBTN = (Button) findViewById(R.id.host_button);
        this.joinBTN = (Button) findViewById(R.id.join_button);
        this.connectBTN = (Button) findViewById(R.id.connect_button);
        this.disconnectBTN = (Button) findViewById(R.id.disconnectBTN);

        this.startGame = (Button) findViewById(R.id.server_startGame) ;
        this.serverIP = (TextView) findViewById(R.id.serverIP_txt);
        this.log =(TextView) findViewById(R.id.log_txt);
        this.serverIP_cliente = (EditText) findViewById(R.id.serverIP_client);
        this.serverIP_cliente.setText("");

        registerWfdReceiver();

        this.hostBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        connectBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.setText(log.getText() +"\n register ");//check patin

                registerWfdReceiver();

            }
        });

        disconnectBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unRegisterWfdReceiver();
            }
        });
    }
    public void onClick(View view) {
//
        int buttonId = view.getId();

        //open new socket connection and wait for client in inGame
        if(buttonId == R.id.host_button){
            registerWfdReceiver();
            // this.log.setText(this.log.getText() +"\n"+ server.showIP());
        }
        //try to connect to chosen server , if connection istablished and server start game, start game
        if(buttonId == R.id.join_button){
            this.log.setText(this.log.getText() +"\n"+ this.serverIP_cliente.getText());//check patin
            onDiscover();
        }
    }



    public void onChannelDisconnected(){
        Toast.makeText(this,"WiFi Direct Disconnected - Reinitialize.",Toast.LENGTH_SHORT).show();
        reinitializeChannel();
    }

    private void reinitializeChannel(){
        mChannel = mManager.initialize(this, getMainLooper(), this);
        Toast.makeText(this, "WiFi Direct Channel Initialization: " + ((mChannel != null)? "SUCCESS" : "FAILED"), Toast.LENGTH_SHORT).show();
    }

    private void registerWfdReceiver(){
        wfdReceiver = new WiFiDirectReceiver(mManager, mChannel, this);
        wfdReceiver.registerReceiver();
    }

    private void unRegisterWfdReceiver(){
        if(wfdReceiver != null)
            wfdReceiver.unregisterReceiver();
        wfdReceiver = null;
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