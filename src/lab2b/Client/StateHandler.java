package lab2b.Client;

import lab2b.Client.State.*;

public class StateHandler {
    private ClientState currentState;
    private Client client;
    public StateHandler(Client client){
        this.client = client;
        currentState = new StateIdle();
    }

    public void PrintState(){
        currentState.PrintState();
    }

    public void InvokeStartCalling(){
        currentState = currentState.StartCalling(client);
    }

    public void InvokeReceiveCall(){
        currentState = currentState.ReceiveCall(client);
    }

    public void InvokeCallAccepted(){
        currentState = currentState.CallAccepted(client);
    }

    public void InvokeCallConfirmation(){
        currentState = currentState.CallConfirmation(client);
    }

    public void InvokeEndSession(){
        currentState = currentState.EndSession(client);
    }

    public void InvokeAbortSession(){
        currentState = currentState.AbortSession(client);
    }

    public void InvokeEndSessionConfirmation(){
        currentState = currentState.EndSessionConfirmation(client);
    }
}
