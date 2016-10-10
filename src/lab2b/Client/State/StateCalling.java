package lab2b.Client.State;

import lab2b.Client.Client;

public class StateCalling extends ClientState{
    @Override
    public ClientState CallConfirmation(Client client) {
        return new StateInSession();
    }

    @Override
    public void PrintState() {
        System.out.println("Calling");
    }
}
