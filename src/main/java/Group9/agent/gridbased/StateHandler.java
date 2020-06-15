package Group9.agent.gridbased;

import Interop.Action.GuardAction;
import Interop.Percept.GuardPercepts;

public interface StateHandler {

    public ActionContainer<GuardAction> execute(GuardPercepts percepts, GridBased agent);

    public StateType getNextState();

    public void resetState();

}
