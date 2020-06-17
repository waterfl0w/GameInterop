package Group9.agent.Guard2;
import Group9.Game;
import Interop.Action.*;
import Interop.Agent.Guard;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Geometry.Point;
import Interop.Percept.GuardPercepts;
import Interop.Percept.Smell.SmellPercept;
import Interop.Percept.Smell.SmellPerceptType;
import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;
import Interop.Percept.Vision.VisionPrecepts;

import java.util.Set;

public class CampingAgent implements Guard {

    /*
        Find target area
        Roam around it until intruder is sighted
     */

    //Currently unused
    private Point location;
    private Angle rotation; //0 rotation -> positive Y, neutral X
    private boolean foundIntruder = false;
    private boolean foundTargetArea = false;
    private boolean IntruderCapture = false;
    private boolean set = false;

    public CampingAgent() {
        location = new Point(0,0);
        rotation = Angle.fromRadians(0);
    }

    @Override
    public GuardAction getAction(GuardPercepts percepts) {

        VisionPrecepts vision = percepts.getVision();
        Set<ObjectPercept> objectPercepts = vision.getObjects().getAll();

        if(foundIntruder){
            return doIntruderChaseAction(percepts);
        } else {
            for (ObjectPercept object : objectPercepts) {
                if (object.getType().equals(ObjectPerceptType.Intruder)) {
                    foundIntruder = true;
                    break;
                }
            }
        }

        if(foundTargetArea){
            // 1. Go into the target area, and spin
            // 2. Walk around the target area
            return doTargetAreaAction(percepts);
        } else {
            for(ObjectPercept object : objectPercepts){
                if (object.getType().equals(ObjectPerceptType.TargetArea)){
                    foundTargetArea = true;
                    break;
                }
            }

            if(!percepts.wasLastActionExecuted())
            {
                Angle newRotation = Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble());
                return new Rotate(newRotation);
            }
            else
            {
                Distance movingDistance = new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * 0.5);
                double newY = location.getY() + Math.sin(rotation.getRadians())*movingDistance.getValue();
                double newX = location.getX() + Math.cos(rotation.getRadians())*movingDistance.getValue();
                location = new Point(newX, newY);
                return new Move(movingDistance);
            }
        }


    }

    private GuardAction doTargetAreaAction(GuardPercepts percepts) {
        if(!set) {
            Set<ObjectPercept> objects = percepts.getVision().getObjects().getAll();
            for (ObjectPercept object :
                    objects) {
                if (object.getType().equals(ObjectPerceptType.TargetArea)) {
                    Move move = new Move(new Distance(object.getPoint(), new Point(0, 0)));
                    if (move.getDistance().getValue() >= percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue()) {
                        move = new Move(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard());
                    }
                    double newY = location.getY() + Math.sin(rotation.getRadians())*move.getDistance().getValue();
                    double newX = location.getX() + Math.cos(rotation.getRadians())*move.getDistance().getValue();
                    location = new Point(newX, newY);

                    return move;
                }
            }
            set = true;
        }
        Angle newRotation = Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble());
        rotation = Angle.fromRadians(rotation.getRadians()+newRotation.getRadians());
        if(rotation.getDegrees() > 360){
            rotation = Angle.fromDegrees(rotation.getDegrees() - 360);
        } else if(rotation.getDegrees() < 0){
            rotation = Angle.fromDegrees(rotation.getDegrees() + 360);
        }
        return new Rotate(newRotation);
    }

    private GuardAction doIntruderChaseAction(GuardPercepts percepts) {
        return new NoAction();
    }

    private boolean checkGuardWin(GuardPercepts percepts){
        Set<ObjectPercept> guardView = percepts.getVision().getObjects().getAll();
        Distance captureDistance = percepts.getScenarioGuardPercepts().getScenarioPercepts().getCaptureDistance();

        for(ObjectPercept object : guardView){
            if(object.getType().equals(ObjectPerceptType.Intruder)){
                foundIntruder = true;
                Distance dis = new Distance(location,object.getPoint());
                if(dis.equals(captureDistance)){
                    IntruderCapture = true;
                }

            }
        }
                if(foundIntruder && IntruderCapture){
                    return true;
                }
       return false;
    }

     }
