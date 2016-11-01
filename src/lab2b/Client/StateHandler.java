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

    public void InvokeStartCalling(String receiveUser) throws Exception{
        currentState = currentState.StartCalling(receiveUser,client);
    }

    public void InvokeReceiveCall(String msg) throws Exception{
        currentState = currentState.ReceiveCall(msg,client);
    }

    public void InvokeCallAccepted(String user){
        currentState = currentState.CallAccepted(user,client);
    }

    public void InvokeCallConfirmation(String user,String msg) throws Exception{
        currentState = currentState.CallConfirmation(user,client,msg);
    }

    public void InvokeEndSession(String user) throws Exception{
        currentState = currentState.EndSession(user,client);
    }

    public void InvokeAbortSession(String user) throws Exception{
        currentState = currentState.AbortSession(user,client);
    }

    public void InvokeEndSessionConfirmation(){
        currentState = currentState.EndSessionConfirmation();
    }

    public void InvokeCancel(String user) throws Exception{
        currentState = currentState.Cancel(user,client);
    }

    public void InvokeResetState(){
        currentState = currentState.ResetState();
    }

    public void InvokeResetState(String msg){
        currentState = currentState.ResetState(msg);
    }
}
