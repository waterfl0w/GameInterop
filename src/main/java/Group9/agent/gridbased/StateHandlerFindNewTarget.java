package Group9.agent.gridbased;

import Group9.agent.deepspace.ActionContainer;
import Group9.agent.odyssey.GridMap;
import Group9.math.Vector2;
import Group9.tree.PointContainer;
import Interop.Action.GuardAction;
import Interop.Action.Move;
import Interop.Action.NoAction;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StateHandlerFindNewTarget implements StateHandler{
    private GridBased agent;
    private StateType nextState;
    private boolean active;

    private boolean debug = true;

    private final Queue<ActionContainer<GuardAction>> actionsQueue = new LinkedList<>();

    private final int TELEPORT_PRIORITY_TURNS = 100;
    private int teleportPriorityChange = -1;

    private boolean initialRoundAfterTeleport = false;

    private boolean testSquare = false;

    Map<ObjectPerceptType, Integer> targetTimeout;
    Map<ObjectPerceptType, Integer> originalPriority;

    private final int DEFAULT_TARGET_TIMEOUT = 25;

    List<ObjectPerceptType> targetsPriority = new ArrayList<>(Arrays.asList(
            ObjectPerceptType.TargetArea,
            ObjectPerceptType.SentryTower,
            ObjectPerceptType.Door,
            ObjectPerceptType.Window,
            ObjectPerceptType.Teleport,
            ObjectPerceptType.EmptySpace
    ));

    public StateHandlerFindNewTarget() {
        targetTimeout = targetsPriority.stream().collect(Collectors.toMap(Function.identity(), t -> 0));
        originalPriority = targetsPriority.stream().collect(Collectors.toMap(Function.identity(), t -> targetsPriority.indexOf(t)));
    }

    @Override
    public ActionContainer<GuardAction> execute(GuardPercepts percepts, GridBased agent) {
        Group9.agent.deepspace.ActionContainer<GuardAction> retAction = Group9.agent.deepspace.ActionContainer.of(this, new NoAction());

        this.agent = agent;

        initialRoundAfterTeleport = percepts.getAreaPercepts().isJustTeleported() && !initialRoundAfterTeleport && teleportPriorityChange == -1;
        if(initialRoundAfterTeleport)
        {
            this.teleportPriorityChange = TELEPORT_PRIORITY_TURNS;
        }

        if (!active) {
            // add actions to queue to get to new best target
            if(testSquare)
            {
                Vector2 target = this.agent.getPosition().add(this.agent.getDirection().rotated(Math.PI / 2).mul(4));
                actionsQueue.addAll(
                        this.agent.moveTowardsPoint(percepts, this.agent.getDirection(), this.agent.getPosition(), target)
                );
            }
            else
            {
                findNewTarget(percepts);
                active = true;
            }
        }

        // if this is an ongoing state, execute queue
        if (!actionsQueue.isEmpty()) {
            retAction = actionsQueue.poll();

            // --- check for collision
            if(retAction.getAction() instanceof Move && false)
            {
                Move action = (Move) retAction.getAction();

                {
                    Vector2 position = new Vector2.Origin();
                    Vector2 direction = new Vector2(0, 1).normalise();

                    final double length = action.getDistance().getValue() + 0.5D;
                    final Vector2 end = direction.mul(length);
                    PointContainer.Line line = new PointContainer.Line(position, end);

                    final Vector2 move = direction.mul(length);

                    Vector2 pointA = position.add(line.getNormal());
                    Vector2 pointB = pointA.add(move);
                    Vector2 pointD = position.sub(line.getNormal());
                    Vector2 pointC = pointD.add(move);

                    PointContainer.Polygon quadrilateral = new PointContainer.Polygon(pointA, pointB, pointC, pointD);

                    boolean collision = percepts.getVision().getObjects()
                            .filter(e -> e.getType().isSolid())
                            .getAll().stream().anyMatch(e -> quadrilateral.isPointInside(Vector2.from(e.getPoint())));

                    if(collision)
                    {
                        retAction = Group9.agent.deepspace.ActionContainer.of(this, new NoAction(), Group9.agent.deepspace.ActionContainer.Input.create().i("collision", true));
                        actionsQueue.clear();
                    }
                }
            }

        }

        postExecute();
        return retAction;
    }

    @Override
    public StateType getNextState() {
        return nextState;
    }

    private void postExecute() {
        if(testSquare) {
            nextState = StateType.FIND_NEW_TARGET;
            return;
        }
        if (actionsQueue.isEmpty()) {
            nextState = StateType.EXPLORE_360;
            active = false;
        } else {
            nextState = StateType.FIND_NEW_TARGET;
        }
    }

    public void resetState()  {
        this.actionsQueue.clear();
        this.active = false;
    }

    Set<CellPosition> blackList = new HashSet<>();
    private void findNewTarget(GuardPercepts guardPercepts)
    {

        tickDowngradeTimeout();

        if (guardPercepts.getAreaPercepts().isInDoor())  downgradeTarget(ObjectPerceptType.Door);
        if (guardPercepts.getAreaPercepts().isInSentryTower())  downgradeTarget(ObjectPerceptType.SentryTower);
        if (guardPercepts.getAreaPercepts().isInWindow())  downgradeTarget(ObjectPerceptType.Window);
        if (guardPercepts.getAreaPercepts().isJustTeleported())  downgradeTarget(ObjectPerceptType.Teleport);

       //TODO
        GridMap gridMap = agent.getGridMap();
        CellContent[][] cells = gridMap.getCells();
        CellContent leastSeenCell = null;

        CellPosition agentPosition = gridMap.toCell(agent.getPosition().getX(), agent.getPosition().getY());

        for(int y = -5; y <= 5; y++)
        {
            for(int x = -5; x <= .t.5; x++)
            {
                int rx = agentPosition.x() + x;
                int ry = agentPosition.y() + y;

                CellContent cell = cells[ry][rx];

                if(cell != null)
                {
                    if(leastSeenCell == null ||
                        (leastSeenCell.getLogValue() < cell.getLogValue() && cell.getLogValue() < 0 && !blackList.contains(cell.getCellPosition()))
                    )
                    {
                        leastSeenCell = cell;
                    }
                }
            }
        }

        blackList.add(leastSeenCell.getCellPosition());
        Vector2 target = gridMap.toRealWorld(leastSeenCell.getCellPosition());
        actionsQueue.addAll(agent.moveTowardsPoint(guardPercepts, agent.getDirection(), agent.getPosition(), target));
        /*List<Vector2> shortestPath =  gridMap.path(agent.getPosition(), target);
        for (int i = 0; i < shortestPath.size() - 2; i++) {
            Vector2 s = shortestPath.get(0 + i);
            Vector2 c = shortestPath.get(1 + i);
            Vector2 n = shortestPath.get(2 + i);
            actionsQueue.addAll(agent.moveTowardsPoint(guardPercepts, c.sub(s).normalise(), c, n));
        }*/
    }


    /**
     * Moves 'target' to the bottom of the target priority list
     * @param target
     */
    private void downgradeTarget(ObjectPerceptType target) {
        targetTimeout.put(target, DEFAULT_TARGET_TIMEOUT);
        targetsPriority.remove(target);
        targetsPriority.add(target);
    }

    private void tickDowngradeTimeout() {
        for (ObjectPerceptType objectType : targetTimeout.keySet()) {
            int curTimeout = targetTimeout.get(objectType);
            if (curTimeout == 1) {
                // return it to its original priority
                targetsPriority.remove(objectType);
                targetsPriority.add(originalPriority.get(objectType), objectType);
            }
            if (curTimeout > 0) {
                targetTimeout.put(objectType, curTimeout - 1);
            }
        }
    }
}
