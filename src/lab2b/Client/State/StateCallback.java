package lab2b.Client.State;

public class StateCallback extends ClientState{
    @Override
    public ClientState CallAccepted() {
        return new StateInSession();
    }

    @Override
    public void PrintState() {
        System.out.println("Callback");
    }

    @Override
    public State GetState() {
        return State.CALLBACK;
    }
}
