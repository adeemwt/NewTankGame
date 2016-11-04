package com.example.adeem.tankgame;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Connection;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class multiPlayerInGame extends AppCompatActivity {
    TextView txt;
    Thread socketServerThread;
    ObjectInputStream input;
    ObjectOutputStream output;
    private ServerSocket server;
    private Socket connection;
    WifiP2pInfo wifiP2pInfo;
    boolean flag = true;

    Button test;
    Button start;
    boolean isServer = false;
    Intent intent;
    multiPlayerInGame appActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player_in_game);
        txt = (TextView) findViewById(R.id.log_ingameMulti);
        test = (Button) findViewById(R.id.send_test);
        start = (Button) findViewById(R.id.button_start_multi);

        test.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag = false;
                txt.setText("pressses!!!!!!!!!!!!");
            }
        });



        Bundle bundle = getIntent().getExtras();

        wifiP2pInfo = (WifiP2pInfo) bundle.get("WIFI_P2P_INFO");
        if (wifiP2pInfo != null && wifiP2pInfo.isGroupOwner) {
            txt.setText(txt.getText() +"i am the server!!!");
            isServer = true;
//            socketServerThread = new Thread(new ServerSocketThread());
//            socketServerThread.start();
            intent = new Intent(appActivity, server_inGame.class);//start game!!! (multi ingame)
            appActivity.startActivity(intent);
        }
        else {
            // game.setMyTurn(false);
            try {
                Thread.sleep(100); //waiting 0.1 seconds for host to set up his socket 			    server before connecting.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //txt.setText(txt.getText() +"i am the client!!!");

            intent = new Intent(appActivity, Client_Ingame_trial.class);//start game!!! (multi ingame)
            appActivity.startActivity(intent);

//            socketServerThread = new Thread(new ClientSocketThread());
//            socketServerThread.start();
        }

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isServer) {
                    try {
                        output.writeObject(new String("fuck"));

                        output.flush();
                    } catch (IOException ioException) {
                        //ioException.printStackTrace();
                        txt.setText(txt.getText() +"\n" + ioException.toString());

                    }
                }
            }
        });
    }
    private void sendMessage(Object message){
        try{
            output.writeObject(message);
            output.flush();
        }
        catch (IOException ioException){
            ioException.printStackTrace();
        }
    }



    class ClientSocketThread extends Thread {
        Intent intent;
        Socket conn;
        @Override
        public void run() { //the client socket thread can send the outout stearm and input to the ingame
            try {
               conn = new Socket();
                conn.bind(null);
                InetSocketAddress  isa = new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
                conn.connect(isa, 100000);


                output = new ObjectOutputStream(conn.getOutputStream());
                output.flush();
                input = new ObjectInputStream(conn.getInputStream());

                Intent i = new Intent();
                Bundle b = new Bundle();
                ArrayList<Socket> conns = new ArrayList();
                conns.add(conn);
//                b.putParcelable("CONNECTION",conns);
//                i.putExtras(b);
//                i.setClass(this, SearchDetailsActivity.class);
//                startActivity(i);

                intent = new Intent(appActivity, Client_Ingame_trial.class);//start game!!! (multi ingame)
                //intent.putExtra("CONNECTION", conns);
                appActivity.startActivity(intent);


                //each client gets here , they connect to the server and here they can call the ingame class and pass the streams

                String message = "";

                try {
                    message = (String)input.readObject();
                    txt.setText(txt.getText() + "\n" + message);
                } catch (ClassNotFoundException classNotFoundException) {
                        classNotFoundException.printStackTrace();
                }

            } catch (EOFException eofException) {
                //   displayToast("Connection closed.");
                //    exit();
            } catch (IOException ioException) {
                ioException.printStackTrace();
                //   exit();
            } finally {
               // closeStreams();
                try {
                    Thread.sleep(50); // this is it
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    public class ServerSocketThread extends Thread {

        @Override
        public void run() {
            ArrayList<Socket> allSockets = new ArrayList<>();
            Intent intent;
            try {
                server = new ServerSocket(WiFiDirectReceiver.PORT);//, 1);
                while(flag) {
                    try {
                        connection = server.accept();
                        allSockets.add(connection);

                        //tempporry check
                        intent = new Intent(appActivity, server_inGame.class);//start game!!! (multi ingame)
                        intent.putExtra("TANKS_CONNECTIONS", allSockets);
                        //txt.setText("now going to the intent!!!!!!!!!!!!!!");
                        appActivity.startActivity(intent);
                        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }

                intent = new Intent(appActivity, server_inGame.class);//start game!!! (multi ingame)
                intent.putExtra("TANKS_CONNECTIONS", allSockets);
                txt.setText("now going to the intent!!!!!!!!!!!!!!");
                appActivity.startActivity(intent);
                //send the array to the server in game using intent
            } catch (IOException ioException) {
                ioException.printStackTrace();
             //   exit();
            }
        }
    }

    private void waitForConnection() throws IOException {
        connection = server.accept();
    }

    private void closeStreams() {
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void connectToServer() throws IOException {
        connection = new Socket();
        connection.bind(null);
        //connection = new Socket(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
        InetSocketAddress  isa = new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
        connection.connect(isa, 100000);

    }

    /**
     * during the game play receive information
     *
     * @throws IOException
     */
    private void whilePlaying() throws IOException {
        Object message = "";
        do {
            try {
                message = input.readObject();
                txt.setText(txt.getText() + "\n" + ((String)message).toString());
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        } while (!message.toString().equals("END"));
    }

    /**
     * get stream to send and receive data
     *
     * @throws IOException
     */
    private void setupStreams() throws IOException {
        txt.setText(txt.getText() +"\ninitializing the streams .... ");
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        txt.setText(txt.getText() +"\n finished initializing the streams .... ");
    }


}


