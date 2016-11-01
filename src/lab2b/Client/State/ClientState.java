package lab2b.Client.State;

import lab2b.Client.Client;

import java.util.Timer;
import java.util.TimerTask;

public abstract class ClientState {
    public ClientState(){
        PrintState();
        Timer timer = new Timer();
        int timeout = 30000;
        TimerTask action = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Timeout was invoked.\nWas in state " + GetState().toString());
                ResetState();
            }
        };
        timer.schedule(action,timeout);
    }
    public ClientState(String msg){
        PrintState();
        System.out.println(msg);
    }
    public ClientState StartCalling(String receiveUser,Client client) throws Exception{return new StateIdle();}
    public ClientState ReceiveCall(String msg,Client client) throws Exception{return new StateIdle();}
    public ClientState CallAccepted(String user, Client client){return new StateIdle();}
    public ClientState CallConfirmation(String user,Client client,String msg) throws Exception{return new StateIdle();}
    public ClientState EndSession(String user,Client client) throws Exception{return new StateIdle();}
    public ClientState AbortSession(String user,Client client) throws Exception{return new StateIdle();}
    public ClientState EndSessionConfirmation(){return new StateIdle();}
    public ClientState Cancel(String user,Client client) throws Exception{
        client.Send("SIP CANCEL "+user);
        return new StateIdle();
    }
    public ClientState ResetState(){return new StateIdle();}
    public ClientState ResetState(String msg){
        if (msg.contains("SIP CANCEL")){
            String[] split = msg.split("SIP CANCEL");
            if (split.length==2)
                System.out.println(split[1]);
        }
        return new StateIdle();
    }
    void PrintState(){System.out.println(this.toString());}
    public State GetState(){return State.UNKNOWN;}
}


