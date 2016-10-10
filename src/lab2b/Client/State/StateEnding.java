package lab2b.Client.State;

import lab2b.Client.Client;

public class StateEnding extends ClientState{
    @Override
    public ClientState EndSessionConfirmation(Client client) {
        return new StateIdle();
    }

    @Override
    public void PrintState() {
        System.out.println("Ending");
    }
}
