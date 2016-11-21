package classes;

import android.widget.TextView;

import com.firebase.client.core.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by ITSIK on 29/10/2016.
 */



public class Server{
    private boolean flag = true;
    private ServerSocket serverSocket;
    TextView log ;

    public void start(TextView logg) {
        this.log = logg;
        log.setText(log.getText() +"tank ");
        new Thread(new Runnable() {
            public void run() {
                try { // Create a server socket
                    serverSocket = new ServerSocket(8000);
                    log.setText(log.getText() +"initializing socket" + getIPAddress(true));//if the socket is open it wont take it

                    // Listen for a connection request
                    while (flag) {
                        Socket socket = serverSocket.accept();
                        Thread thread = new ThreadClass(socket);

                        log.setText(log.getText() +"new Client canncted to server");
                        thread.start();

                    }
                    // Create data input and output streams
                    serverSocket.close();
                } catch (IOException | ClassNotFoundException ex) {
                    log.setText(log.getText() +"cannot connect to server"); //this doesnt change the log
                    return;
                }
            }
        }).start();
    }
    public String showIP(){
        return  serverSocket.getInetAddress().toString();
    }
    private class ThreadClass extends Thread {


        private Socket socket;
        private ObjectOutputStream outputToClient;
        private ObjectInputStream inputFromClient;

        public ThreadClass(Socket socket) throws IOException, ClassNotFoundException {
            this.socket = socket;
            outputToClient = new ObjectOutputStream(socket.getOutputStream());
            inputFromClient = new ObjectInputStream(socket.getInputStream());
            log.setText(log.getText() +"waiting for client name ..."); // this doesn't change the log

            String name = inputFromClient.readObject().toString(); // get client name
            boolean flag = true;
            if (flag) {
            }
        }

        @Override
        public void run() {
            while (true) { // Receive radius from the client
                try {
                    Object msg = inputFromClient.readObject();
                    log.setText(log.getText() +"the name is : "+ msg.toString());
                } catch (IOException | ClassNotFoundException e1) {
                    return;
                }
            }
        }


        // send the message that the user wanted to send to all (if he did not
        // specify a specific user
        private void notifyAll(Object msg) throws IOException {
        }
    }

    public void close(){
        if(serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }


}
