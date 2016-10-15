package lab2b.Client;

import lab2b.Client.State.*;

class StateHandler {
    private ClientState currentState;
    private final Client client;
    public StateHandler(Client client){
        this.client = client;
        currentState = new StateIdle();
    }

    public State InvokeGetState(){return currentState.GetState();}

    public void InvokeStartCalling(String receiveUser){
        currentState = currentState.StartCalling(receiveUser,client);
    }

    public void InvokeReceiveCall(String msg){
        currentState = currentState.ReceiveCall(msg,client);
    }

    public void InvokeCallAccepted(){
        currentState = currentState.CallAccepted();
    }

    public void InvokeCallConfirmation(String user){
        currentState = currentState.CallConfirmation(user,client);
    }

    public void InvokeEndSession(String user){
        currentState = currentState.EndSession(user,client);
    }

    public void InvokeAbortSession(String user){
        currentState = currentState.AbortSession(user,client);
    }

    public void InvokeEndSessionConfirmation(){
        currentState = currentState.EndSessionConfirmation();
    }

    public void InvokeCancel(String user){
        currentState = currentState.Cancel(user,client);
    }

    public void InvokeResetState(){
        currentState = currentState.ResetState();
    }
}
