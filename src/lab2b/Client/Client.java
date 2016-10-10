package lab2b.Client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Client {
    private Socket socket;
    private BufferedReader inFromUser;
    private boolean run = true;
    private DataOutputStream toServer;
    private StateHandler sh;

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
            toServer = new DataOutputStream(socket.getOutputStream());
            sh = new StateHandler(this);
            System.out.print("Enter nickname: ");
            msg = inFromUser.readLine();
            toServer.writeBytes("/nick " + msg + '\n');
            new Receive(socket).start();
            while (run){
                msg = inFromUser.readLine();
                handleMessage(msg);
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

    private void handleMessage(String msg){
        switch (msg.toUpperCase()){
            case "SC":
                sh.InvokeStartCalling();
                break;

            case "CC":
                sh.InvokeCallConfirmation();
                break;

            case "AS":
                sh.InvokeAbortSession();
                break;

            case "ES":
                sh.InvokeEndSession();
                break;

            case "ESC":
                sh.InvokeEndSessionConfirmation();
                break;

            case "RC":
                sh.InvokeReceiveCall();
                break;

            case "CA":
                sh.InvokeCallAccepted();
                break;

            case "STATE":
                sh.PrintState();
                break;

            default:
                Send(msg);
        }
    }

    public void Send(String msg){
        try {
            toServer.writeBytes(msg + '\n');
        }catch (IOException e){
            e.printStackTrace();
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
