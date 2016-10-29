package classes;

import android.widget.TextView;

import com.firebase.client.core.Platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ITSIK on 29/10/2016.
 */



public class Server{
    private boolean flag = true;
    private ServerSocket serverSocket;
    TextView log ;
    public void start(TextView logg) {
        this.log = logg;
        new Thread(new Runnable() {
            public void run() {
                try { // Create a server socket
                    serverSocket = new ServerSocket(8000);
                    // Listen for a connection request
                    while (flag) {
                        Socket socket = serverSocket.accept();
                        Thread thread = new ThreadClass(socket);
                        thread.start();
                    }
                    // Create data input and output streams
                    serverSocket.close();
                } catch (IOException | ClassNotFoundException ex) {
                    log.setText(log.getText() +"cannot connect to server");
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
            String name = inputFromClient.readObject().toString(); //sjould we get vlient name ?
            boolean flag = true;
            if (flag) {
            }
        }

        @Override
        public void run() {
            while (true) { // Receive radius from the client
                try {
                    Object msg = inputFromClient.readObject();
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

}
