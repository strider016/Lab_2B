package lab2b.Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by rasmusjansson on 2016-09-26.
 */
public class Client {
    private Socket socket;
    private BufferedReader inFromUser;
    private boolean run = true;

    public static void main(String[] args) throws IOException{
        if(args.length != 2) {
            System.out.println("Usage: java Client <server_addr> <server_port>");
            System.exit(0);
        }
        Client client = new Client();
        client.Start(args);
    }

    public void Start(String[] args) throws IOException{
        String addr = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            String msg;
            InetAddress address = InetAddress.getByName(addr);
            socket = new Socket(address,port);
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
            new Receive(socket).start();
            while (run){
                msg = inFromUser.readLine();
                toServer.writeBytes(msg + '\n');
            }
        }catch (SocketException e){
            System.out.println("Server is unreachable.");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }finally {
            socket.close();
            System.exit(0);
        }
    }

    class Receive extends Thread{
        private Socket socket;
        public Receive(Socket socket){this.socket = socket;}
        public void run(){
            try {
                String responseMsg;
                boolean run = true;
                BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (run){
                    responseMsg = sin.readLine();
                    if (responseMsg != null)
                        System.out.println(responseMsg);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
