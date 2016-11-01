package lab2b.Client.State;

import lab2b.Client.Client;

public class StateCallback extends ClientState{
    @Override
    public ClientState CallAccepted(String user,Client client) {
        client.setStream();
        client.connectTo();
        client.startStream();
        System.out.println("The conversation has begun with + " + user);
        return new StateInSession();
    }

    @Override
    public void PrintState() {
        System.out.println("Callback");
    }

    @Override
    public State GetState() {
        return State.CALLBACK;
    }
}
