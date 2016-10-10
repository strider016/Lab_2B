package lab2b.Client.State;

import lab2b.Client.Client;

public class StateCallback extends ClientState{
    @Override
    public ClientState CallAccepted(Client client) {
        return new StateInSession();
    }

    @Override
    public void PrintState() {
        System.out.println("Callback");
    }
}
