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
        this.occupiedCells = new HashSet<>();
    }

    private Set<Vector2> cells360;

    private Set<Vector2> occupiedCells;

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
            // RxR
            Vector2 objectPosition = Vector2.from(objectSeen.getPoint()).rotated(-agent.getDirection().getClockDirection()).add(agent.getPosition());
            // List of RxR coordinates to beginning of cells
            List<Vector2> cellsInLine = gridMap.ray(agent.getPosition(), objectPosition);
            cellsInLine.remove(objectPosition);
            for (Vector2 cellPosition : cellsInLine) {
                gridMap.update(cellPosition.getX(), cellPosition.getY(), false, ObjectPerceptType.EmptySpace);
            }
            // we keep a list of cells that had a wall in them
            if (objectSeen.getType().isSolid()) {
                // eesh.. no time to rewrite this
                occupiedCells.add(gridMap.toRealWorld(gridMap.toCell(objectPosition.getX(), objectPosition.getY())));
            }
            cells360.addAll(cellsInLine);
            gridMap.update(objectPosition.getX(), objectPosition.getY(), objectSeen.getType().isSolid(), objectSeen.getType());
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
            cleanTargetableCells();
            active = false;
        } else {
            nextState = StateType.EXPLORE_360;
        }
    }

    // inits the graph (or adds a new vertex)  &  schedules rotations
    private void init(GuardPercepts percepts) {
        actionsQueue.addAll(agent.planRotation(percepts, Math.PI * 2));
        cells360.clear();
        occupiedCells.clear();
    }

    private void cleanTargetableCells() {
        cells360.removeAll(occupiedCells);
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

    private void debugPrintVector2s(Set<Vector2> l) {
        for (Vector2 v : l)  {
            System.out.println(v.getX()  + " " + v.getY());
        }
    }
}
