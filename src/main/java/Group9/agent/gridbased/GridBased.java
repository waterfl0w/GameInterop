package Group9.agent.gridbased;

import Group9.agent.deepspace.ActionContainer;
import Group9.agent.odyssey.GridMap;
import Group9.math.Vector2;
import Interop.Action.GuardAction;
import Interop.Action.Move;
import Interop.Action.NoAction;
import Interop.Action.Rotate;
import Interop.Agent.Guard;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Scenario.SlowDownModifiers;
import Interop.Percept.Sound.SoundPercept;
import Interop.Percept.Sound.SoundPerceptType;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;
import Interop.Percept.Vision.VisionPrecepts;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.DoubleFunction;

public class GridBased implements Guard {
    private Vector2 position = new Vector2(0, 0);
    private Vector2 direction = new Vector2(0, 1).normalise();

    double cellLen = 0.5;
    private ActionContainer<GuardAction> lastAction = null;
    private StateType curState;
    private final EnumMap<StateType, StateHandler> stateHandlers;

    private GridMap gridMap = new GridMap(cellLen, 50, 50);

    public GridBased() {
        curState = StateType.INITIAL;
        stateHandlers = new EnumMap<>(StateType.class);

        // Maps 'StateType' to 'instance of StateType state handler class'
        EnumSet.allOf(StateType.class).forEach(t -> {
            try {
                stateHandlers.put(t, t.getStateHandlerClass().getConstructor().newInstance());
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        });
    }

    protected GridMap getGridMap()
    {
        return this.gridMap;
    }

    int frame = 0;
    @Override
    public GuardAction getAction(GuardPercepts percepts) {
        //--- activity map
        CellContent cellContent = gridMap.get(position.getX(), position.getY());
        if(cellContent != null && percepts.wasLastActionExecuted() && false)
        {
            getGridMap().writeDebugImage(String.format("frames/%d.png", frame), false, false);
            frame++;
            cellContent.countVisit();
        }
        //---

        ActionContainer<GuardAction> actionToDo = ActionContainer.of(this, new NoAction());
        VisionPrecepts vision = percepts.getVision();
        Set<ObjectPercept> objectPercepts = vision.getObjects().getAll();

        assert percepts.wasLastActionExecuted() : "Last action executed wasn't valid!";

        // if we moved/rotated, we update our current status
        if (percepts.wasLastActionExecuted() && lastAction != null) {
            if (lastAction.getAction() instanceof Move) {
                move(((Move) lastAction.getAction()).getDistance().getValue());
            } else if (lastAction.getAction() instanceof Rotate) {
                rotate(((Rotate) lastAction.getAction()).getAngle().getRadians());
            }
        }

        // main loop
        do {
            actionToDo = stateHandlers.get(curState).execute(percepts, this);
            curState = stateHandlers.get(curState).getNextState();
        } while (actionToDo.getAction() instanceof Inaction || actionToDo.getAction() == null);

        lastAction = actionToDo;
        return actionToDo.getAction();
    }

    public Vector2 canSeeIntruder(GuardPercepts percepts)
    {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();

        if(!intruders.isEmpty())
        {
            Vector2 centre = new Vector2.Origin();
            for(ObjectPercept e : intruders)
            {
                centre = centre.add(Vector2.from(e.getPoint()));
            }
            return centre.mul(1D/intruders.size());
        }

        return null;
    }

    public double canHearSuspiciousSound(GuardPercepts percepts)
    {
        Set<SoundPercept> sounds = percepts.getSounds()
                .filter(e -> e.getType() == SoundPerceptType.Noise)
                .getAll();

        if(!sounds.isEmpty())
        {
            double soundDirection = 0;
            for(SoundPercept e : sounds)
            {
                soundDirection += e.getDirection().getRadians();
            }
            return soundDirection / sounds.size();
        }

        return -1;
    }

    protected Queue<ActionContainer<GuardAction>> moveTowardsPoint(GuardPercepts percepts, Vector2 direction, Vector2 source,
                                                                   Vector2 target) {
        Queue<ActionContainer<GuardAction>> retActionsQueue = new LinkedList<>();

        Vector2 desiredDirection = target.sub(source);
        double rotationDiff = direction.angledSigned(desiredDirection);
        if (Math.abs(rotationDiff) > 1E-1) {
            retActionsQueue.addAll(planRotation(percepts, rotationDiff));
        }

        final double maxAllowedMove = percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * getSpeedModifier(percepts);
        final double distance = target.distance(source);
        final int fullMoves = (int) (distance / maxAllowedMove);
        final double remainder = distance % percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue();

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

    public boolean hasSuspicion(GuardPercepts percepts)
    {
        return canSeeIntruder(percepts) != null || canHearSuspiciousSound(percepts) != -1;
    }

    public double getSpeedModifier(GuardPercepts guardPercepts)
    {
        SlowDownModifiers slowDownModifiers =  guardPercepts.getScenarioGuardPercepts().getScenarioPercepts().getSlowDownModifiers();
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

    protected Queue<ActionContainer<GuardAction>> planRotation(GuardPercepts percepts, double alpha)
    {
        // TODO kinda cheating; fix
        final double sign = Math.signum(alpha);

        Queue<ActionContainer<GuardAction>> retActionsQueue = new LinkedList<>();

        double maxRotation = percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * sign;
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

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public Vector2 getDirection() {
        return direction;
    }

    public void setDirection(Vector2 direction) {
        this.direction = direction;
    }

    public EnumMap<StateType, StateHandler> getStateHandlers() {
        return stateHandlers;
    }

    public Set<Vector2> getCellsInLine(Vector2 agentOrigin, Point seenObject) {
        Vector2 v = Vector2.from(seenObject).rotated(-direction.getClockDirection());
        Vector2 dv = v.normalise().mul(cellLen * 0.1);

        Set<Vector2> cellPositions = new HashSet<>();

        for (double tick = 0; tick <= v.length()/(cellLen * 0.1); tick++) {
            cellPositions.add(agentOrigin.add(dv.mul(tick)));
        }

        return cellPositions;
    }


    private List<CellPosition> getCellIntercepts(Point agentOrigin, Point seenObject) {
        // Idea for another way of computing
        // Wasted too much time on this, and it's pribably not even faster.
        // Not finished, missing cases where agentOrigin > seenObject

        // line equation for this ray
        double m = seenObject.getY() / seenObject.getX();
        double mInv = seenObject.getX() / seenObject.getY();
        DoubleFunction<Double> f_x = x -> m * (x - agentOrigin.getX()) + agentOrigin.getY();
        DoubleFunction<Double> f_y = y -> mInv * (y - agentOrigin.getY()) + agentOrigin.getX();

        if (Math.abs(seenObject.getY()) < Math.abs(seenObject.getX())) {
            for (double x = agentOrigin.getX(); x <= seenObject.getX(); x += cellLen) {
                double y = f_x.apply(x);
                System.out.println(x + ", " + y);
                System.out.println("Cell: " + "[" + Math.floor(x / cellLen) + ", " + Math.floor(y / cellLen) + "]");
            }
        } else {
            for (double y = agentOrigin.getY(); y <= seenObject.getY(); y += cellLen) {
                double x = f_y.apply(y);
                System.out.println(x + ", " + y);
                System.out.println("Cell: " + "[" + Math.floor(x / cellLen) + ", " + Math.floor(y / cellLen) + "]");
            }
        }

        return null;
    }
}
