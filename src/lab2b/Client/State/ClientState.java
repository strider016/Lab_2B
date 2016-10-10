package lab2b.Client.State;

import lab2b.Client.Client;

public abstract class ClientState {
    public ClientState StartCalling(Client client){return this;}
    public ClientState ReceiveCall(Client client){return this;}
    public ClientState CallAccepted(Client client){return this;}
    public ClientState CallConfirmation(Client client){return this;}
    public ClientState EndSession(Client client){return this;}
    public ClientState AbortSession(Client client){return this;}
    public ClientState EndSessionConfirmation(Client client){return this;}
    public void PrintState(){}
}


