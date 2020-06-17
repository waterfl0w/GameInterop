package Group9.agent.Guard2;

import Group9.Game;
import Group9.agent.deepspace.ActionContainer;
import Group9.math.Vector2;
import Interop.Action.GuardAction;
import Interop.Action.Move;
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

public class Tactical implements Guard {
    private Angle rotation;
    private boolean FoundArea = false;
    private Queue<ActionContainer<GuardAction>> CampingArea = new LinkedList<>();
    private Queue<ActionContainer<GuardAction>> followIntruder = new LinkedList<>();
    private Vector2 position;
    private int NoOfRotations = 10;
    private int count = 0;

    private boolean foundIntruder = false;


    public Tactical() {
        position = new Vector2.Origin();
        rotation = Angle.fromRadians(0);
    }

    @Override
    public GuardAction getAction(GuardPercepts percepts) {
        VisionPrecepts vision = percepts.getVision();
        Set<ObjectPercept> objectPercepts = vision.getObjects().getAll();

//        if(!foundIntruder)
//        {
//            for(ObjectPercept object : objectPercepts)
//            {
//                if(object.getType().equals(ObjectPerceptType.Intruder))
//                {
//                    foundIntruder= true;
//                    break;
//                }
//            }
//        }
//        if(foundIntruder)
//        {
//            if(followIntruder.isEmpty()){
//                followIntruder.addAll(predictIntruderMove(percepts));
//            }
//            return followIntruder.poll().getAction();
//        }
//   if(!FoundArea)
//        {
//            for(ObjectPercept object : objectPercepts){
//                if (object.getType().equals(ObjectPerceptType.Teleport) ||object.getType().equals(ObjectPerceptType.ShadedArea) || object.getType().equals(ObjectPerceptType.SentryTower) || object.getType().equals(ObjectPerceptType.Window) || object.getType().equals(ObjectPerceptType.Door)){
//                    FoundArea = true;
//                    break;
//                }
//            }
//        }
//        if(FoundArea && percepts.wasLastActionExecuted()){
//
//                 if (CampingArea.isEmpty()) {
//                     CampingArea.addAll(camping(percepts));
//                 }
//                 while (count < NoOfRotations) {
//
//                     return CampingArea.poll().getAction();
//                 }
//
//             }


        return exploration(percepts);
    }


    private GuardAction exploration(GuardPercepts percepts) {
        /**Set<ObjectPercept> objectPercepts = percepts.getVision().getObjects().getAll();

         for(ObjectPercept object : objectPercepts) {
         if (object.getType().equals(ObjectPerceptType.Wall)) {
         return new Rotate(Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble()));
         }
         }

         return new Move(new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * getSpeedModifier(percepts)));
         */
        if (!percepts.wasLastActionExecuted()) {
            Angle newRotation = Angle.fromRadians(
                    percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble()
            );
            return new Rotate(newRotation);
        } else {

            return new Move(new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * getSpeedModifier(percepts)));
        }
    }

    private Queue<ActionContainer<GuardAction>> camping(GuardPercepts percepts) {
        Set<ObjectPercept> guardView = percepts.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.Teleport) || e.getType().equals(ObjectPerceptType.ShadedArea) || e.getType().equals(ObjectPerceptType.SentryTower) || e.getType().equals(ObjectPerceptType.Window) || e.getType().equals(ObjectPerceptType.Door) && !e.getType().equals(ObjectPerceptType.Wall))
                .getAll();
        Vector2 max = null;
        final Vector2 viewingDirection = new Vector2(0, 1);
        for (ObjectPercept object : guardView) {
            Vector2 tmp = Vector2.from(object.getPoint());
            if (max == null || max.length() < tmp.length() && viewingDirection.angle(max) > viewingDirection.angle(tmp)) {
                max = tmp;

            }
        }
        if (max != null && max.length() >= 0.1) {
            return moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), max);
        }
        Angle newRotation = Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble());


        rotation = Angle.fromRadians(rotation.getRadians() + newRotation.getRadians());
        if (rotation.getDegrees() > 360) {

            rotation = Angle.fromDegrees(rotation.getDegrees() - 360);
            count++;

        } else if (rotation.getDegrees() < 0) {

            rotation = Angle.fromDegrees(rotation.getDegrees() + 360);
            count++;

        }

        Queue<ActionContainer<GuardAction>> actions = new LinkedList<>();
        actions.add(ActionContainer.of(this, new Rotate(rotation)));

        return actions;

    }


    private Queue<ActionContainer<GuardAction>> predictIntruderMove(GuardPercepts percepts) {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();

        if (!intruders.isEmpty()) {
            Vector2 one = new Vector2.Origin();
            Vector2 two = new Vector2.Origin();
            Vector2 guard = new Vector2.Origin();
            List<Vector2> intPos = new ArrayList<>();
            for (ObjectPercept o : intruders) {
                intPos.add(Vector2.from(o.getPoint()));
            }
            if (intPos.size() == 1) {
                return moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), intPos.get(0));
            } else {
                for (int i = 0; i < intPos.size() - 1; i++) {
                    one = intPos.get(i);
                    two = intPos.get(i + 1);
                    double distance1 = one.distance(two);
                    return moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), two.sub(one).mul(distance1).add(guard));
                }
            }
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

    protected Queue<ActionContainer<GuardAction>> moveTowardsPoint(GuardPercepts percepts, Vector2 direction, Vector2 source,
                                                                   Vector2 target) {
        Queue<ActionContainer<GuardAction>> retActionsQueue = new LinkedList<>();

        Vector2 desiredDirection = target.sub(source);
        double rotationDiff = direction.angledSigned(desiredDirection);
        if (Math.abs(rotationDiff) > 1E-1) {
            retActionsQueue.add(ActionContainer.of(this, new Rotate(Angle.fromRadians(rotationDiff))));
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

    private double getSpeedModifier(GuardPercepts guardPercepts) {
        SlowDownModifiers slowDownModifiers = guardPercepts.getScenarioGuardPercepts().getScenarioPercepts().getSlowDownModifiers();
        if (guardPercepts.getAreaPercepts().isInWindow()) {
            return slowDownModifiers.getInWindow();
        } else if (guardPercepts.getAreaPercepts().isInSentryTower()) {
            return slowDownModifiers.getInSentryTower();
        } else if (guardPercepts.getAreaPercepts().isInDoor()) {
            return slowDownModifiers.getInDoor();
        }
        return 1;
    }
}