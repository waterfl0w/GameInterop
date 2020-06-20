package Group9.agent.gridbased;

import Group9.agent.deepspace.ActionContainer;
import Group9.agent.odyssey.GridMap;
import Group9.math.Vector2;
import Interop.Action.GuardAction;
import Interop.Action.Move;
import Interop.Geometry.Distance;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.*;
import java.util.stream.Collectors;

public class StateHandlerExplore360 implements StateHandler {

    private final Queue<ActionContainer<GuardAction>> actionsQueue = new LinkedList<>();
    private GridBased agent;
    private StateType nextState = StateType.EXPLORE_360;

    // 'false -> true' after first use of this state
    private boolean active = false;

    public StateHandlerExplore360() {
        this.cells360 = new HashSet<>();
    }

    private Set<Vector2> cells360;

    @Override
    public ActionContainer<GuardAction> execute(GuardPercepts percepts, GridBased agent) {
        ActionContainer<GuardAction> retAction = ActionContainer.of(this, new Inaction());
        this.agent = agent;

        if(agent.hasSuspicion(percepts))
        {
            this.nextState = StateType.FOLLOW_INTRUDER;
            this.resetState();
            return ActionContainer.of(this, new Inaction());
        }

        if (!active) {
            init(percepts);
            active = true;
        }

        if (!actionsQueue.isEmpty()) {
            retAction = actionsQueue.poll();
        }

        GridMap gridMap = agent.getGridMap();
        // add information from this fov to grid
        Set<ObjectPercept> objectPercepts = percepts.getVision().getObjects().getAll();
        for (ObjectPercept objectSeen : objectPercepts) {
            //Set<Vector2> cellsInLine = agent.getCellsInLine(agent.getPosition(), objectSeen.getPoint());
            Vector2 objectCellPosition = Vector2.from(objectSeen.getPoint()).rotated(-agent.getDirection().getClockDirection()).add(agent.getPosition());
            List<Vector2> cellsInLine = gridMap.ray(agent.getPosition(), objectCellPosition);
            cellsInLine.remove(objectCellPosition);
            for (Vector2 cellPosition : cellsInLine) {
                gridMap.update(cellPosition.getX(), cellPosition.getY(), false, ObjectPerceptType.EmptySpace);
            }
            cells360.addAll(cellsInLine);
            gridMap.update(objectCellPosition.getX(), objectCellPosition.getY(), objectSeen.getType().isSolid(), objectSeen.getType());
        }

        //System.out.println(gridMap);
        postExecute();
        return retAction;
    }

    public Set<Vector2> getCells360() {
        return cells360;
    }

    void postExecute() {
        if (actionsQueue.isEmpty()) {
            nextState = StateType.FIND_NEW_TARGET;
            active = false;
        } else {
            nextState = StateType.EXPLORE_360;
        }
    }

    // inits the graph (or adds a new vertex)  &  schedules rotations
    private void init(GuardPercepts percepts) {
        actionsQueue.addAll(agent.planRotation(percepts, Math.PI * 2));
        cells360.clear();
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
