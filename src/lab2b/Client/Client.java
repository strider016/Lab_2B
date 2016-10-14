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
    private String username;
    private boolean ReceivedTRO = false;
    private boolean InSession = false;
    private String toUsername;

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
            username = inFromUser.readLine();
            toServer.writeBytes("/nick " + username + '\n');
            new Receive(socket).start();
            while (run){
                msg = inFromUser.readLine();
                handleMessage(msg);
            }
        }catch (SocketException e){
            System.out.println("Server is unreachable.");
        }catch (IOException e){
            System.out.println(e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            socket.close();
            System.exit(0);
        }
    }

    private void handleMessage(String msg){
        if (msg.startsWith("/call ")){
            toUsername = msg.substring("/call ".length());
            sh.InvokeStartCalling(toUsername);
        }
        else if (ReceivedTRO && msg.equals("y")) {
            sh.InvokeCallAccepted();
            InSession = true;
        }
        else if (ReceivedTRO && msg.equals("n"))
            sh.InvokeCancel(toUsername);
        else if (InSession && msg.equals("/end"))
            sh.InvokeEndSession(toUsername);
        else
            Send(msg);
    }

    public void Send(String msg){
        try {
            toServer.writeBytes(msg + '\n');
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return username;
    }

    public String getExternalIp(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException e){
            e.printStackTrace();
            return null;
        }
    }

    public void setInSession(){
        InSession=true;
    }

    class Receive extends Thread{
        private Socket socket;
        private String sendUsername;
        public Receive(Socket socket){this.socket = socket;}
        public void run(){
            try {
                String responseMsg;
                boolean run = true;
                BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (run){
                    responseMsg = sin.readLine();
                    if (responseMsg != null) {
                        if (responseMsg.startsWith("SIP")) {
                            handleMessage(responseMsg);
                        }else
                            System.out.println(responseMsg);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void handleMessage(String msg){
            SIPCommand type = getSIPCommand(msg.toUpperCase());
            String[] array;
            String username;
            switch (type){
                case INVITE:
                    array = msg.split(" ");
                    toUsername = array[3];
                    InetAddress ipaddress = null;
                    try{
                        ipaddress = InetAddress.getByName(array[5]);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    int port = Integer.parseInt(array[6]);

                    //System.out.println("Incoming call");
                    //System.out.print("Do you wanna accept? ");
                    sh.InvokeReceiveCall(toUsername);
                    break;

                case TRO:
                    ReceivedTRO = true;
                    sh.InvokeCallConfirmation(toUsername);
                    break;

                case ACK:
                    InSession = true;
                    sh.InvokeCallAccepted();
                    break;

                case BYE:
                    ReceivedTRO = false;
                    InSession = false;
                    sh.InvokeAbortSession();
                    break;

                case OK:
                    ReceivedTRO = false;
                    InSession = false;
                    sh.InvokeEndSessionConfirmation();
                    break;

                default:;
            }
        }

        private SIPCommand getSIPCommand(String msg){
            if(msg.startsWith("SIP INVITE"))
                return SIPCommand.INVITE;
            else if (msg.startsWith("SIP TRO"))
                return SIPCommand.TRO;
            else if (msg.startsWith("SIP OK"))
                return SIPCommand.OK;
            else if (msg.startsWith("SIP ACK"))
                return SIPCommand.ACK;
            else if (msg.startsWith("SIP BYE"))
                return SIPCommand.BYE;
            return SIPCommand.UNKNOWN;
        }
    }
}
