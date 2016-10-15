package lab2b.Client.State;

public class StateEnding extends ClientState{
    @Override
    public ClientState EndSessionConfirmation() {
        return new StateIdle();
    }

    @Override
    public void PrintState() {
        System.out.println("Ending");
    }

    @Override
    public State GetState() {
        return State.ENDING;
    }
}
