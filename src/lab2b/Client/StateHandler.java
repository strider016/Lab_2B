package lab2b.Client;

import lab2b.Client.State.*;

public class StateHandler {
    private ClientState currentState;
    private Client client;
    public StateHandler(Client client){
        this.client = client;
        currentState = new StateIdle();
    }

    public void InvokePrintState(){
        currentState.PrintState();
    }

    public void InvokeStartCalling(String receiveUser){
        currentState = currentState.StartCalling(receiveUser,client);
    }

    public void InvokeReceiveCall(String msg){
        currentState = currentState.ReceiveCall(msg,client);
    }

    public void InvokeCallAccepted(){
        currentState = currentState.CallAccepted(client);
    }

    public void InvokeCallConfirmation(String user){
        currentState = currentState.CallConfirmation(user,client);
    }

    public void InvokeEndSession(String user){
        currentState = currentState.EndSession(user,client);
    }

    public void InvokeAbortSession(){
        currentState = currentState.AbortSession(client);
    }

    public void InvokeEndSessionConfirmation(){
        currentState = currentState.EndSessionConfirmation(client);
    }

    public void InvokeCancel(String user){
        currentState = currentState.Cancel(user,client);
    }
}
