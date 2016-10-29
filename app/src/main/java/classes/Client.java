package classes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by ITSIK on 29/10/2016.
 */
public class Client {

    private ObjectOutputStream toServer = null;
    private ObjectInputStream fromServer = null;
    private Socket socket;
    public void start() {
    }
    private void enterChatRoom() {
        try { // Create a socket to connect to the server
            // InetAddress host = InetAddress.getLocalHost();
                socket = new Socket("localhost", 8000);
                toServer = new ObjectOutputStream(socket.getOutputStream());
                fromServer = new ObjectInputStream(socket.getInputStream());
                new ServerListener(fromServer,toServer).start();

        } catch (SocketException e1) {
            try {
                socket.close();
                socket.getInetAddress();
            } catch (IOException e2) {
            } catch (NullPointerException nll) {
            }
        } catch (IOException e3) {
        }
    }
    private void sendMessege() {

    }

    private class ServerListener extends Thread {
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;

        public ServerListener(ObjectInputStream objectInputStream,ObjectOutputStream objectOut0utStream) {
            this.objectInputStream = objectInputStream;
            this.objectOutputStream = objectOut0utStream;
            try{
                this.objectOutputStream.writeObject(new String("myname"));

            }catch (Exception e){

            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (true) {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
