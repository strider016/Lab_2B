package lab2b.Client.State;

import lab2b.Client.Client;

import java.net.InetAddress;

public class StateCalling extends ClientState{
    @Override
    public ClientState CallConfirmation(String user,Client client,String msg) {
        try {
            String[] array = msg.split(" ");
            try {
                client.setRemoteIpAddress(InetAddress.getByName(array[2]));
            }catch (Exception e){
                e.printStackTrace();
            }
            client.setStream();
            client.connectTo();
            client.startStream();
            System.out.println("The conversation har begun with user: " + user);
            client.Send("SIP ACK " + user);
            return new StateInSession();
        }catch (Exception e){
            return ResetState();
        }
    }

    @Override
    public void PrintState() {
        System.out.println("Calling");
    }

    @Override
    public State GetState() {
        return State.CALLING;
    }
}
