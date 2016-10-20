package lab2b.Client.State;

import lab2b.Client.Client;

public abstract class ClientState {
    ClientState(){
        PrintState();
    }
    public ClientState StartCalling(String receiveUser,Client client) throws Exception{return new StateIdle();}
    public ClientState ReceiveCall(String msg,Client client) throws Exception{return new StateIdle();}
    public ClientState CallAccepted(){return new StateIdle();}
    public ClientState CallConfirmation(String user,Client client) throws Exception{return new StateIdle();}
    public ClientState EndSession(String user,Client client) throws Exception{return new StateIdle();}
    public ClientState AbortSession(String user,Client client) throws Exception{return new StateIdle();}
    public ClientState EndSessionConfirmation(){return new StateIdle();}
    public ClientState Cancel(String user,Client client) throws Exception{
        client.Send("SIP CANCEL "+user);
        return new StateIdle();
    }
    public ClientState ResetState(){return new StateIdle();}
    void PrintState(){System.out.println(this.toString());}
    public State GetState(){return State.UNKNOWN;}
}


