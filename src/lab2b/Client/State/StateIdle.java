package lab2b.Client.State;

import lab2b.Client.Client;

public class StateIdle extends ClientState{
    @Override
    public ClientState StartCalling(Client client) {
        client.Send("Hello, i'm starting to call!");
        return new StateCalling();
    }

    @Override
    public ClientState ReceiveCall(Client client) {
        return new StateCallback();
    }

    @Override
    public void PrintState(){
        System.out.println("Idle");
    }
}
