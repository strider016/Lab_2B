package lab2b.Client.State;

import lab2b.Client.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ThreadLocalRandom;

public class StateIdle extends ClientState{
    @Override
    public ClientState StartCalling(String receiveUser,Client client) {
        client.Send("SIP INVITE " + receiveUser + " " + client.getUsername() +
                " #" + receiveUser + " " + client.getExternalIp() + " " + generatePort());
        return new StateCalling();
    }

    @Override
    public ClientState ReceiveCall(String msg,Client client) {
        client.Send("SIP TRO " + msg);
        return new StateCallback();
    }

    @Override
    public void PrintState(){
        System.out.println("Idle");
    }

    private int generatePort(){
        int startRange = 6000;
        int endRange = 6050;
        int port;
        ServerSocket ss = null;
        try {
            port = ThreadLocalRandom.current().nextInt(startRange,endRange+1);
            ss = new ServerSocket(port);
            return port;
        }catch (IOException e){
            generatePort();
        }finally {
            if (ss != null)
                try {
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return -1;
    }
}
