package com.example.adeem.tankgame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import classes.Client;
import classes.Server;

public class multiplayer_mainPage extends AppCompatActivity implements View.OnClickListener{

    private Button hostBTN;
    private Button joinBTN;
    private TextView serverIP;
    private TextView log;
    private EditText serverIP_cliente;
    private Button startGame;

    private static final String IPADDRESS_PATTERN =
                    "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_main_page);
        this.hostBTN = (Button) findViewById(R.id.host_button);
        this.joinBTN = (Button) findViewById(R.id.join_button);
        this.startGame = (Button) findViewById(R.id.server_startGame) ;
        this.serverIP = (TextView) findViewById(R.id.serverIP_txt);
        this.log =(TextView) findViewById(R.id.log_txt);
        this.serverIP_cliente = (EditText) findViewById(R.id.serverIP_client);
        this.serverIP_cliente.setText("");
        this.hostBTN.setOnClickListener(this);
        this.joinBTN.setOnClickListener(this);

    }
    @Override
    public void onClick(View view) {

        int buttonId = view.getId();
        Intent startAct2Intent;

        //open new socket connection and wait for client in inGame
        if(buttonId == R.id.host_button){
            this.joinBTN.setVisibility(View.INVISIBLE);
            this.serverIP_cliente.setVisibility(View.INVISIBLE);
            this.startGame.setVisibility(View.VISIBLE);
            this.log.setText("SERVER MODE\nWaiting for Clients...");
            Server server = new Server();
            server.start(this.log);

           // this.log.setText(this.log.getText() +"\n"+ server.showIP());
        }
        //try to connect to chosen server , if connection istablished and server start game, start game
        if(buttonId == R.id.join_button){
            this.log.setText(this.log.getText() +"\n"+ this.serverIP_cliente.getText());//check patin
            if(Pattern.matches(IPADDRESS_PATTERN,this.serverIP_cliente.getText().toString())){
                this.hostBTN.setVisibility(View.INVISIBLE);
                this.serverIP.setVisibility(View.INVISIBLE);
                this.log.setText("CLIENT MODE\nConnecting to server....");
                Client client = new Client();
                client.start(this.log,serverIP.toString());
            }
            else{
                Toast.makeText(getApplicationContext(),"Please enter Server IP address", Toast.LENGTH_SHORT).show();
            }

        }
    }
}