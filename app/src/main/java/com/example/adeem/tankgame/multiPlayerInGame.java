package com.example.adeem.tankgame;

import android.net.wifi.p2p.WifiP2pInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class multiPlayerInGame extends AppCompatActivity {
    TextView txt;
    Thread socketServerThread;
    ObjectInputStream input;
    ObjectOutputStream output;
    private ServerSocket server;
    private Socket connection;
    WifiP2pInfo wifiP2pInfo;
    Button test;
    boolean isServer = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_player_in_game);
        txt = (TextView) findViewById(R.id.log_ingameMulti);
        Bundle bundle = getIntent().getExtras();
        test = (Button) findViewById(R.id.send_test);

        wifiP2pInfo = (WifiP2pInfo) bundle.get("WIFI_P2P_INFO");
        if (wifiP2pInfo != null && wifiP2pInfo.isGroupOwner) {
            txt.setText(txt.getText() +"i am the server!!!");
            isServer = true;
            socketServerThread = new Thread(new ServerSocketThread());
            socketServerThread.start();
        }
        else {
            // game.setMyTurn(false);
            try {
                Thread.sleep(100); //waiting 0.1 seconds for host to set up his socket 			    server before connecting.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            txt.setText(txt.getText() +"i am the client!!!");
            socketServerThread = new Thread(new ClientSocketThread());
            socketServerThread.start();
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


    class ClientSocketThread2 extends Thread { // not in use
        @Override
        public void run() {
            try {
                connectToServer();
                setupStreams();
                whilePlaying();
            } catch (EOFException eofException) {
             //   displayToast("Connection closed.");
            //    exit();
            } catch (IOException ioException) {
                ioException.printStackTrace();
             //   exit();
            } finally {
                closeStreams();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class ServerSocketThread2 extends Thread { // noy in use

        @Override
        public void run() {
            try {
                server = new ServerSocket(WiFiDirectReceiver.PORT);//, 1);
                while (true) {
                    try {
                        waitForConnection();
                        txt.setText(txt.getText() +"\nOK 1111111111111111111111 !");
                        setupStreams();
                        txt.setText(txt.getText() +"\n2@@@@@@@@@@@@O@@K!");
                        whilePlaying();
                    } catch (EOFException eofException) {
                        //   displayToast("Server Ended the connection.");
                        //   exit();
                    } finally {
                        closeStreams();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                //   exit();
            }
        }
    }
    class ClientSocketThread extends Thread {
        @Override
        public void run() {
            try {
                connection = new Socket();
                connection.bind(null);
                //connection = new Socket(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
                InetSocketAddress  isa = new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
                connection.connect(isa, 100000);


                output = new ObjectOutputStream(connection.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connection.getInputStream());


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
                closeStreams();
                try {
                    Thread.sleep(50); // this is it
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ServerSocketThread extends Thread {

        @Override
        public void run() {
            try {
                server = new ServerSocket(WiFiDirectReceiver.PORT);//, 1);
                    try {
                        connection = server.accept();


                        output = new ObjectOutputStream(connection.getOutputStream());
                        output.flush();
                        input = new ObjectInputStream(connection.getInputStream());


                        String message = "this was sent from the server";
                        output.writeObject(message);
                        output.flush();
                    } catch (EOFException eofException) {
                     //   displayToast("Server Ended the connection.");
                     //   exit();
                    } finally {
                        closeStreams();

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
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


