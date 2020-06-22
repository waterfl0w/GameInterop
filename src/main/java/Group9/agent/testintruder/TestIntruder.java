package Group9.agent.testintruder;

import Group9.agent.deepspace.ActionContainer;
import Group9.math.Vector2;
import Interop.Action.*;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Percept.GuardPercepts;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Scenario.SlowDownModifiers;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class TestIntruder implements Intruder {

    private Vector2 position = new Vector2(0, 0);
    private Vector2 direction = new Vector2(0, 1).normalise();

    private final Queue<ActionContainer<IntruderAction>> actionsQueue = new LinkedList<>();

    private List<Vector2> targetPositions = new LinkedList<>();
    private Vector2 centreTargetArea;
    private GridMap gridMap;

    private List<Vector2> currentPath;

    private ActionContainer<IntruderAction> lastAction = null;

    @Override
    public IntruderAction getAction(IntruderPercepts percepts) {
        if(!percepts.wasLastActionExecuted() && gridMap != null)
        {
            this.currentPath.clear();
            this.currentPath = gridMap.path(this.position, centreTargetArea);
            this.currentPath.remove(0);
        }

        // if we moved/rotated, we update our current status
        if (percepts.wasLastActionExecuted() && lastAction != null) {
            if (lastAction.getAction() instanceof Move) {
                move(((Move) lastAction.getAction()).getDistance().getValue());
            } else if (lastAction.getAction() instanceof Rotate) {
                rotate(((Rotate) lastAction.getAction()).getAngle().getRadians());
            }
        }

        if(gridMap != null)
        {
            Set<ObjectPercept> objectPercepts = percepts.getVision().getObjects().getAll();
            for (ObjectPercept objectSeen : objectPercepts) {
                //Set<Vector2> cellsInLine = agent.getCellsInLine(agent.getPosition(), objectSeen.getPoint());
                Vector2 objectCellPosition = Vector2.from(objectSeen.getPoint()).rotated(-direction.getClockDirection()).add(position);
                List<Vector2> cellsInLine = gridMap.ray(position, objectCellPosition);
                cellsInLine.remove(objectCellPosition);
                for (Vector2 cellPosition : cellsInLine) {
                    if(gridMap.get(cellPosition.getX(), cellPosition.getY()) != 2)
                    {
                        gridMap.set(cellPosition, s(1));
                    }
                }
                gridMap.set(objectCellPosition, objectSeen.getType().isSolid() ? s(2) : s(0));
            }
        }

        if(actionsQueue.isEmpty() && currentPath != null && !currentPath.isEmpty())
        {
            Vector2 point = currentPath.remove(0);
            this.actionsQueue.addAll(moveTowardsPoint(percepts, this.direction, this.position, point));
        }


        if(actionsQueue.isEmpty() && targetPositions.size() <= 2 && (currentPath == null || currentPath.isEmpty()))
        {
            double angle = percepts.getTargetDirection().getRadians() - Math.PI / 2;
            if(targetPositions.size() == 0)
            {
                this.targetPositions.add(new Vector2(Math.cos(angle), Math.sin(angle)));
                //this.actionsQueue.add(ActionContainer.of(this, new Rotate(Angle.fromRadians(Math.PI / 4))));
                this.actionsQueue.add(ActionContainer.of(this, new Move(new Distance(0.1))));
            }
            else if(targetPositions.size() == 1)
            {
                assert percepts.wasLastActionExecuted();
                this.targetPositions.add(new Vector2(Math.cos(angle), Math.sin(angle)).add(position));

                //(d-b)/(a-c)
                // ax+b
                // cx+d
                Vector2 alpha = this.targetPositions.get(0);
                Vector2 beta = this.targetPositions.get(1);
                double a = (alpha.getY() - 0) / (alpha.getX() - 0);
                double b = -(a * alpha.getX() - alpha.getY());

                double c = (beta.getY() - position.getY()) / (beta.getX() - position.getX());
                double d = -(c * beta.getX() - beta .getY());
                double x = (d-b)/(a-c);
                double y = a * x + b;

                this.centreTargetArea = new Vector2(x, y);
                this.gridMap = new GridMap(0.5, 50, 50);
                this.gridMap.set(centreTargetArea, s(9));
                this.gridMap.set(position, s(8));
            }
            else
            {
                this.currentPath = gridMap.path(position, centreTargetArea);
                this.currentPath.remove(0);
            }
        }

        if(actionsQueue.isEmpty())
        {
            return new NoAction();
        }
        return (this.lastAction = actionsQueue.poll()).getAction();
    }

    protected Queue<ActionContainer<IntruderAction>> moveTowardsPoint(IntruderPercepts percepts, Vector2 direction, Vector2 source,
                                                                   Vector2 target) {
        Queue<ActionContainer<IntruderAction>> retActionsQueue = new LinkedList<>();

        Vector2 desiredDirection = target.sub(source).normalise();
        double rotationDiff = direction.angledSigned(desiredDirection);
        if (Math.abs(rotationDiff) > 1E-1) {
            retActionsQueue.addAll(planRotation(percepts, rotationDiff));
        }

        final double maxAllowedMove = percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder().getValue() * getSpeedModifier(percepts);
        final double distance = target.distance(source);
        final int fullMoves = (int) (distance / maxAllowedMove);
        final double remainder = distance % percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder().getValue();

        ActionContainer.Input input = ActionContainer.Input.create()
                .i("direction", direction).i("source", source).i("target", target).i("maxAllowedMove", maxAllowedMove)
                .i("distance", distance).i("fullMoves", fullMoves).i("remainder", remainder);
        for (int i = 0; i < fullMoves; i++) {
            retActionsQueue.add(
                    ActionContainer.of(this, new Move(new Distance(maxAllowedMove)), input.clone().i("#fullMoves-i", i))
            );
        }
        if (remainder > 0) {
            retActionsQueue.add(
                    ActionContainer.of(this, new Move(new Distance(remainder)), input.clone().i("#remainder", remainder))
            );
        }

        return retActionsQueue;
    }

    public double getSpeedModifier(IntruderPercepts guardPercepts)
    {
        SlowDownModifiers slowDownModifiers =  guardPercepts.getScenarioIntruderPercepts().getScenarioPercepts().getSlowDownModifiers();
        if(guardPercepts.getAreaPercepts().isInWindow())
        {
            return slowDownModifiers.getInWindow();
        }
        else if(guardPercepts.getAreaPercepts().isInSentryTower())
        {
            return slowDownModifiers.getInSentryTower();
        }
        else if(guardPercepts.getAreaPercepts().isInDoor())
        {
            return slowDownModifiers.getInDoor();
        }

        return 1;
    }

    protected Queue<ActionContainer<IntruderAction>> planRotation(IntruderPercepts percepts, double alpha)
    {
        // TODO kinda cheating; fix
        final double sign = Math.signum(alpha);

        Queue<ActionContainer<IntruderAction>> retActionsQueue = new LinkedList<>();

        double maxRotation = percepts.getScenarioIntruderPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * sign;
        int fullRotations = (int) (alpha / maxRotation);
        double restRotation = (alpha % maxRotation) * sign;

        for (int i = 0; i < Math.abs(fullRotations); i++)  {
            retActionsQueue.offer(
                    ActionContainer.of(this, new Rotate(Angle.fromRadians(maxRotation)))
            );
        }

        if (Math.abs(restRotation) > 0) {
            retActionsQueue.offer(
                    ActionContainer.of(this, new Rotate(Angle.fromRadians(restRotation)))
            );
        }

        return retActionsQueue;
    }

    private void move(double distance)
    {
        this.position = this.position.add(this.direction.mul(distance, distance));
    }

    private void rotate(double theta)
    {
        this.direction = direction.rotated(theta);
    }

    public static short s(int s)
    {
        assert s == (short) s;
        return (short)s;
    }
}
