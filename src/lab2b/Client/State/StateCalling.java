package lab2b.Client.State;

import lab2b.Client.Client;

public class StateCalling extends ClientState{
    @Override
    public ClientState CallConfirmation(String user,Client client) {
        client.Send("SIP ACK " + user);
        return new StateInSession();
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
