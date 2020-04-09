package Group9.agent;

import Group9.Game;
import Interop.Action.*;
import Interop.Agent.Guard;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
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

    //TODO: When it finds intruder, it needs to chase it
    private boolean foundIntruder = false;
    private boolean foundTargetArea = false;
    @Override
    public GuardAction getAction(GuardPercepts percepts) {

        VisionPrecepts vision = percepts.getVision();
        Set<ObjectPercept> objectPercepts = vision.getObjects().getAll();

        for(ObjectPercept object : objectPercepts){
            if(object.getType().equals(ObjectPerceptType.Intruder)){
                return doIntruderChaseAction(percepts);
            }
        }

        if(foundTargetArea){
            // 1. Go into the target area, and spin
            // 2. Walk around the target area
            return new DropPheromone(SmellPerceptType.Pheromone1);
        } else {
            for(ObjectPercept object : objectPercepts){
                if (object.getType().equals(ObjectPerceptType.TargetArea)){
                    foundTargetArea = true;
                    break;
                }
            }

            if(!percepts.wasLastActionExecuted())
            {
                return new Rotate(Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble()));
            }
            else
            {
                return new Move(new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue() * 0.5));
            }
        }


    }

    private GuardAction doIntruderChaseAction(GuardPercepts percepts) {
        return new NoAction();
    }
}
