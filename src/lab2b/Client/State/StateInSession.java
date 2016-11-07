package lab2b.Client.State;

import lab2b.Client.Client;

public class StateInSession extends ClientState{
    public StateInSession(){
        super("Currently in session.");
    }
    @Override
    public ClientState EndSession(String user,Client client) throws Exception{
        try {
            client.stopStream();
            client.Send("SIP BYE " + user);
            return new StateEnding();
        }catch (Exception e){
            return ResetState();
        }
    }

    @Override
    public ClientState AbortSession(String user,Client client) throws Exception{
        try {
            client.stopStream();
            client.Send("SIP 200 OK " + user);
            return new StateIdle();
        }catch (Exception e){
            return ResetState();
        }
    }

    @Override
    public void PrintState() {
        System.out.println("InSession");
    }

    @Override
    public State GetState() {
        return State.INSESSION;
    }
}
