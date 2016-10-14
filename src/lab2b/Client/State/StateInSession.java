package lab2b.Client.State;

import lab2b.Client.Client;

public class StateInSession extends ClientState{
    @Override
    public ClientState EndSession(String user,Client client) {
        client.Send("SIP BYE " + user);
        return new StateEnding();
    }

    @Override
    public ClientState AbortSession(Client client) {
        client.Send("SIP 200 OK");
        return new StateIdle();
    }

    @Override
    public void PrintState() {
        System.out.println("InSession");
    }
}
