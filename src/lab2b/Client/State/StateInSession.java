package lab2b.Client.State;

import lab2b.Client.Client;

public class StateInSession extends ClientState{
    @Override
    public ClientState EndSession(Client client) {
        return new StateEnding();
    }

    @Override
    public ClientState AbortSession(Client client) {
        return new StateIdle();
    }

    @Override
    public void PrintState() {
        System.out.println("InSession");
    }
}
