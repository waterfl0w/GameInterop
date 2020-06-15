package Group9.agent.gridbased;

/**
 * Represents the different states. Each state is also associated to a state handler class.
 * Instances of the agent (DeepSpace) can instantiate their own copies of the handlers.
 */
enum StateType {
    INITIAL(StateHandlerInitial.class),
    EXPLORE_360(StateHandlerExplore360.class);

    private final Class<? extends StateHandler> stateHandlerClass;

    StateType(Class<? extends StateHandler> stateHandlerClass) {
        this.stateHandlerClass = stateHandlerClass;
    }

    public Class<? extends StateHandler> getStateHandlerClass() {
        return stateHandlerClass;
    }
}
