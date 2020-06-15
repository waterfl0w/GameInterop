package Group9.agent.gridbased;

import Group9.math.Vector2;
import Group9.math.graph.Vertex;
import Interop.Action.GuardAction;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Vision.ObjectPercept;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static Interop.Percept.Vision.ObjectPerceptType.EmptySpace;

public class StateHandlerExplore360 implements StateHandler {

    private final Queue<ActionContainer<GuardAction>> actionsQueue = new LinkedList<>();
    private GridBased agent;
    private StateType nextState = StateType.EXPLORE_360;

    // 'false -> true' after first use of this state
    private boolean active = false;

    public StateHandlerExplore360() {
    }

    @Override
    public ActionContainer<GuardAction> execute(GuardPercepts percepts, GridBased agent) {
        ActionContainer<GuardAction> retAction = ActionContainer.of(this, new Inaction());
        this.agent = agent;

        if (!active) {
            init(percepts);
            active = true;
        }

        if (!actionsQueue.isEmpty()) {
            retAction = actionsQueue.poll();
        }

        // add information from this fov to grid
        Set<ObjectPercept> objectPercepts = percepts.getVision().getObjects().getAll();
        for (ObjectPercept objectSeen : objectPercepts) {
            Set<Cell> cellsInLine;
            cellsInLine = agent.getCellsInLine(agent.getPosition(), objectSeen.getPoint());
            Cell objectCell = agent.getCellFromR(Vector2.from(objectSeen.getPoint()));
            cellsInLine.remove(objectCell);
            for (Cell cell : cellsInLine) {
                // TODO: grid.updateCell(cell, EmptySpace);
            }
            //TODO : grid.updateCell(objectCell, objectSeen.getType());
        }

        postExecute();
        return retAction;
    }

    void postExecute() {
        if (actionsQueue.isEmpty()) {
//            nextState = StateType.FIND_NEW_TARGET;
            active = false;
        } else {
            nextState = StateType.EXPLORE_360;
        }
    }

    // inits the graph (or adds a new vertex)  &  schedules rotations
    private void init(GuardPercepts percepts) {
        actionsQueue.addAll(agent.planRotation(percepts, Math.PI * 2));
    }

    @Override
    public StateType getNextState() {
        return this.nextState;
    }

    @Override
    public void resetState() {
        actionsQueue.clear();
        active = false;
    }
}
