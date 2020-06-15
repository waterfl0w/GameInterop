package Group9.agent.gridbased;

import Group9.agent.deepspace.ActionContainer;
import Interop.Action.GuardAction;
import Interop.Percept.GuardPercepts;

public class StateHandlerInitial implements StateHandler {
    StateType nextState;

    @Override
    public ActionContainer<GuardAction> execute(GuardPercepts percepts, GridBased agent) {

        nextState = StateType.EXPLORE_360;

        return ActionContainer.of(this, new Inaction());
    }

    @Override
    public StateType getNextState() {
        return nextState;
    }

    @Override
    public void resetState() {

    }
}
