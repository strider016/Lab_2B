package lab2b.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by rasmusjansson on 2016-09-26.
 */
public class ClientInfo extends Thread{
    private Socket socket;
    private String username;
    private int id;
    private PrintWriter printWriter;
    private BufferedReader reader;
    private boolean run = true;

    public ClientInfo(Socket socket,int id) throws IOException {
        this.socket = socket;
        this.id = id;
        printWriter = new PrintWriter(this.socket.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    public String getUsername() {
        return username;
    }
    
    public int getClientId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void run(){
        try {
            while (run){
                String msg = reader.readLine();
                //System.out.println(msg);
                send(msg);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Client has disconnected.");
    }

    public void send(String msg){
        try {
            printWriter.println(msg.toUpperCase());
            printWriter.flush();
        }catch (NullPointerException e){
            run = false;
        }

    }
}
