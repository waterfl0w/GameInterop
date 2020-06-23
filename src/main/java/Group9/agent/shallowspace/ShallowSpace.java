//package Group9.agent.Guard2;
package Group9.agent.shallowspace;
import Group9.Game;
import Group9.agent.deepspace.ActionContainer;
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
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;
import Interop.Percept.Vision.VisionPrecepts;

import java.util.*;

public class ShallowSpace implements Guard {

    private Angle rotation = Angle.fromRadians(0);
    private boolean foundTeleport = false;
    private boolean foundSentry = false;
    private Queue<ActionContainer<GuardAction>> followIntruder = new LinkedList<>();
    private Queue<ActionContainer<GuardAction>> CampingArea = new LinkedList<>();

    private Vector2[] intruderHistory = new Vector2[2];
     boolean lastSeen = false;
    int NoOfRotation = 40;//10*4
    private int count = 0;

    public ShallowSpace() { }

    @Override
    public GuardAction getAction(GuardPercepts percepts) {

        VisionPrecepts vision = percepts.getVision();
        Set<ObjectPercept> objectPercepts = vision.getObjects().getAll();
        Vector2 intruderPosition = predictIntruder(percepts);

        if(intruderPosition != null || !followIntruder.isEmpty()) {
            if (intruderPosition != null) {
                CampingArea.clear();
                followIntruder.clear();
                if (intruderPosition.equals(new Vector2(0, 0))) {
                    followIntruder.add(ActionContainer.of(this, new NoAction()));
                } else {
                    followIntruder.addAll(
                            moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), intruderPosition)
                    );
                }
            } else {
                intruderHistory = new Vector2[2];
            }

            if (!followIntruder.isEmpty()) {

                return followIntruder.poll().getAction();
            }
        }
        else{

             if (!foundTeleport) {
                for (ObjectPercept object : objectPercepts) {
                    if (object.getType().equals(ObjectPerceptType.Teleport)) {
                        foundTeleport = true;
                        break;
                    }
                }
            }
        if (!foundSentry) {
            for (ObjectPercept object : objectPercepts) {
                if (object.getType().equals(ObjectPerceptType.SentryTower)) {
                    foundSentry = true;
                    break;
                }
            }
        }

        if((foundTeleport || foundSentry)  ) {
            if (CampingArea.isEmpty() ) {
                CampingArea.clear();
                followIntruder.clear();
                CampingArea.addAll(camping(percepts));
            }

             while(count<NoOfRotation) {
                return CampingArea.poll().getAction();
        }
          }
        }

return exploration(percepts);
    }

    private GuardAction exploration(GuardPercepts  percepts)
    {
        if(!percepts.wasLastActionExecuted())
        {
            Angle newRotation = Angle.fromRadians(
                    percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble()
            );
            return new Rotate(newRotation);
        }
        else
        {
            Distance movingDistance = new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * getSpeedModifier(percepts));

            return new Move(movingDistance);
        }
    }

    private double getSpeedModifier(GuardPercepts guardPercepts)
    {
        SlowDownModifiers slowDownModifiers = guardPercepts.getScenarioGuardPercepts().getScenarioPercepts().getSlowDownModifiers();
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
    private Queue<ActionContainer<GuardAction>> camping(GuardPercepts percepts)
    {
        Set<ObjectPercept> guardView = percepts.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.SentryTower) || e.getType().equals(ObjectPerceptType.Teleport) )
                .getAll();
        Vector2 max = null;
        final Vector2 viewingDirection = new Vector2(0, 1);
        for (ObjectPercept object : guardView) {
            Vector2 tmp = Vector2.from(object.getPoint());
            if(max == null || max.length() < tmp.length() && viewingDirection.angle(max) > viewingDirection.angle(tmp))
            {
                max = tmp;
            }
        }

            if(max != null && max.length() >= 0.1) {
         count++;
                return moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), max);
            }

        Angle newRotation = Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble());
        rotation = Angle.fromRadians(rotation.getRadians() + newRotation.getRadians());
        if (rotation.getDegrees() > 360) {
            rotation = Angle.fromDegrees(rotation.getDegrees() - 360);


        } else if (rotation.getDegrees() < 0) {
            rotation = Angle.fromDegrees(rotation.getDegrees() + 360);
        }
        Queue<ActionContainer<GuardAction>> actions = new LinkedList<>();
        actions.add(ActionContainer.of(this, new Rotate(rotation)));
        return actions;

    }

  /*  private GuardAction sentryAction(GuardPercepts pertPoint(),new Point(0,0));
               Move move = new Move(new Distance(o.getPoint(), new Point(0, 0)));
                if (move.getDistance().getValue() >= percep.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue()) {
                    move = new Move(percep.getScenarioGcep)
    {
        Set<ObjectPercept> seeSentry = percep.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.SentryTower) )
                .getAll();

        if (!seeSentry.isEmpty()) {
            for (ObjectPercept o : seeSentry) {
                Distance distance = new Distance(o.geuardPercepts().getMaxMoveDistanceGuard());
                }
                return move;
            }
        }
        Angle newRotation = Angle.fromRadians(percep.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble());
        rotation = Angle.fromRadians(rotation.getRadians()+newRotation.getRadians());
        if(rotation.getDegrees() > 360){
            rotation = Angle.fromDegrees(rotation.getDegrees() - 360);
            count++;
        } else if(rotation.getDegrees() < 0){
            rotation = Angle.fromDegrees(rotation.getDegrees() + 360);
            count++;
        }
        return new Rotate(newRotation);
    }*/



    protected Queue<ActionContainer<GuardAction>> moveTowardsPoint(GuardPercepts percepts, Vector2 direction, Vector2 source,
                                                                   Vector2 target)
    {
        Queue<ActionContainer<GuardAction>> retActionsQueue = new LinkedList<>();

        Vector2 desiredDirection = target.sub(source);
        double rotationDiff = direction.angledSigned(desiredDirection);
        if(Math.abs(rotationDiff) > 1E-1)
        {
            retActionsQueue.add(ActionContainer.of(this, new Rotate(Angle.fromRadians(rotationDiff))));
        }

        final double maxAllowedMove = percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * getSpeedModifier(percepts);
        final double distance = target.distance(source);
        final int fullMoves = (int) (distance / maxAllowedMove);
        final double remainder = distance % percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue();

        ActionContainer.Input input = ActionContainer.Input.create()
                .i("direction", direction).i("source", source).i("target", target).i("maxAllowedMove", maxAllowedMove)
                .i("distance", distance).i("fullMoves", fullMoves).i("remainder", remainder);
        for(int i = 0; i < fullMoves; i++)
        {
            retActionsQueue.add(
                    ActionContainer.of(this, new Move(new Distance(maxAllowedMove)), input.clone().i("#fullMoves-i", i))
            );
        }
        if(remainder > 0)
        {
            retActionsQueue.add(
                    ActionContainer.of(this, new Move(new Distance(remainder)), input.clone().i("#remainder", remainder))
            );
        }

        return retActionsQueue;
    }

    public Vector2 predictIntruder(GuardPercepts percepts)
    {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();

        if (intruders.isEmpty() && lastSeen) {
            intruderHistory[0] = null;
            intruderHistory[1] = null;
        }
        lastSeen = !intruders.isEmpty();

        if(!intruders.isEmpty())
        {
            Vector2 centre = new Vector2.Origin();
            for(ObjectPercept e : intruders)
            {
                centre = centre.add(Vector2.from(e.getPoint()));
            }
            Vector2 position = centre.mul(1D/intruders.size());

            /*
             0: [A,null]
             1: [B,A]
             2: [C,B]
             */

            Vector2 ret = new Vector2(0, 0);
            if (intruderHistory[0] == null) {
                intruderHistory[0] = position;
            } else if (intruderHistory[0] != null && intruderHistory[1] == null) {
                intruderHistory[1] = position;
                ret = intruderHistory[0].add(intruderHistory[0].sub(intruderHistory[1]).mul(1.5));
//                return new Vector2(0, 0);
            } else if (intruderHistory[0] != null && intruderHistory[1] != null) {
                intruderHistory[1] = position;
                intruderHistory[0] = intruderHistory[1];
                ret = intruderHistory[0].add(intruderHistory[0].sub(intruderHistory[1]).mul(1.5));
            }
//
//            if(intruderHistory[1] != null)
//            {
//                Vector2 direction = intruderHistory[0].sub(intruderHistory[1]).normalise();
//                double speed = intruderHistory[1].distance(intruderHistory[0]);
//                return direction.mul(speed).add(direction.flip().mul(0.5));
//            }

            return ret;
        }

        return null;
    }

}