package lab2b.Client;

import lab2b.Client.State.State;
import lab2b.Client.VOIP.MediaStream;

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
    private String toUsername;
    private Receive receive;
    private InetAddress myExternalAddress;
    private int port;
    private InetAddress remoteIpAddress;
    private MediaStream stream;
    private boolean incomingCall = false;

    public static void main(String[] args) throws IOException{
        if(args.length != 2) {
            System.out.println("Usage: java Client <server_addr> <server_port>");
            System.exit(0);
        }
        Client client = new Client();
        client.Start(args);
    }

    public String getUsername() {
        return username;
    }

    public InetAddress getExternalIp(){
        return myExternalAddress;
    }

    public void setToUsername(String toUsername){
        this.toUsername = toUsername;
    }

    public void endClient(){
        run = false;
    }

    public void setStream() {
        try {
            this.stream = new MediaStream(port);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setRemoteIpAddress(InetAddress remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void connectTo(){
        stream.connectTo(remoteIpAddress,port);
    }

    public void startStream(){
        stream.startStream();
    }

    public void stopStream(){
        stream.stopStream();
        stream = null;
    }

    public void setIncomingCall(){
        incomingCall = true;
    }

    public synchronized void Send(String msg) throws Exception{
        toServer.writeBytes(msg + '\n');
    }

    private void Start(String[] args) throws IOException{
        String addr = args[0];
        int port = Integer.parseInt(args[1]);
        try {
            String msg;
            myExternalAddress = InetAddress.getLocalHost();
            InetAddress address = InetAddress.getByName(addr);
            socket = new Socket(address,port);
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            toServer = new DataOutputStream(socket.getOutputStream());
            sh = new StateHandler(this);
            BufferedReader sin = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            initializeSession(sin);
            receive = new Receive(sin,sh,this);
            receive.start();
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
            if (stream != null)
                stream.close();
            socket.close();
        }
        System.exit(0);
    }

    private void initializeSession(BufferedReader sin) throws IOException{
        String responseMsg;
        System.out.print("Enter nickname: ");
        username = inFromUser.readLine();
        toServer.writeBytes("/nick " + username + '\n');
        while (true) {
            responseMsg = sin.readLine();
            if (responseMsg.startsWith("OK Nickname changed"))
                break;
            else if(responseMsg.startsWith("Nickname in use")) {
                System.out.println("Nickname is already taken.");
                System.out.print("Enter nickname: ");
                username = inFromUser.readLine();
                toServer.writeBytes("/nick " + username + '\n');
            }
        }
        System.out.println("Welcome " + username + "!");
    }

    private void handleMessage(String msg) throws Exception{
        if (sh.InvokeGetState() == State.IDLE){
            if (msg.startsWith("/call ")){
                String[] tmp = msg.split(" ");
                if (tmp.length == 2) {
                    toUsername = tmp[1];
                    receive.setSendUsername(toUsername);
                    sh.InvokeStartCalling(toUsername);
                }else
                    System.out.println("Invalid command. /call <user>");
            } else if (incomingCall && msg.startsWith("y")) {
                if (msg.startsWith("y")){
                    incomingCall = false;
                    sh.InvokeReceiveCall(toUsername);
                }else if (msg.startsWith("n")){
                    incomingCall = false;
                    sh.InvokeCancel(toUsername);
                }
            }
        } else if (sh.InvokeGetState() == State.INSESSION && msg.equals("/end"))
            sh.InvokeEndSession(toUsername);
        else
            Send(msg);
    }
}
