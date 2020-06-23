package Group9.agent.gridbased;

import Group9.agent.deepspace.ActionContainer;
import Group9.agent.odyssey.GridMap;
import Group9.math.Vector2;
import Interop.Action.GuardAction;
import Interop.Action.Move;
import Interop.Action.Rotate;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.*;

public class StateHandlerFollowIntruder implements StateHandler {

    private final Queue<ActionContainer<GuardAction>> actionsQueue = new LinkedList<>();
    private StateType nextState = StateType.EXPLORE_360;

    // 'false -> true' after first use of this state
    private boolean active = false;

    public StateHandlerFollowIntruder() { }

    @Override
    public ActionContainer<GuardAction> execute(GuardPercepts percepts, GridBased agent) {
        ActionContainer<GuardAction> retAction = ActionContainer.of(this, new Inaction());

        if (!active) {
            active = true;
            Vector2 target = agent.canSeeIntruder(percepts);
            double soundDirection = agent.canHearSuspiciousSound(percepts);
            if(target != null)
            {
                this.actionsQueue.addAll(agent.moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), target));
            }
            else if(soundDirection != -1)
            {
                this.actionsQueue.add(ActionContainer.of(this, new Rotate(Angle.fromRadians(soundDirection))));
                this.actionsQueue.add(ActionContainer.of(this, new Move(new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * agent.getSpeedModifier(percepts)))));
            }
            else
            {
                this.nextState = StateType.EXPLORE_360;
                this.resetState();
            }
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
            gridMap.update(objectCellPosition.getX(), objectCellPosition.getY(), objectSeen.getType().isSolid(), objectSeen.getType());
        }

        if (!actionsQueue.isEmpty()) {
            retAction = actionsQueue.poll();
        }

        postExecute();
        return retAction;
    }

    void postExecute() {
        if (actionsQueue.isEmpty()) {
            active = false;
        } else {
            nextState = StateType.EXPLORE_360;
        }
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
