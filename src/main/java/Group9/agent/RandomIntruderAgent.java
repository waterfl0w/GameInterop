package Group9.agent;

import Group9.Game;
import Interop.Action.IntruderAction;
import Interop.Action.Move;
import Interop.Action.NoAction;
import Interop.Action.Rotate;
import Interop.Agent.Intruder;
import Interop.Geometry.Angle;
import Interop.Geometry.Distance;
import Interop.Percept.GuardPercepts;
import Interop.Percept.IntruderPercepts;
import Interop.Percept.Scenario.SlowDownModifiers;

public class RandomIntruderAgent implements Intruder {

    private int counter = 0;

    private double getSpeedModifier(IntruderPercepts guardPercepts)
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

    private int randomMoveCounter = 0;

    @Override
    public IntruderAction getAction(IntruderPercepts percepts) {
        double maxDeg = Math.min(Math.PI / 4, Math.abs(percepts.getTargetDirection().getRadians()));
        if(Math.abs(maxDeg) > 1E-02 && randomMoveCounter == 0)
        {
            return new Rotate(Angle.fromRadians(maxDeg * Math.signum(percepts.getTargetDirection().getRadians())));
        }
        else if(randomMoveCounter == 0)
        {
            randomMoveCounter = 10;
        }

        randomMoveCounter--;
        if(!percepts.wasLastActionExecuted())
        {
            return new Rotate(Angle.fromRadians(percepts.getScenarioIntruderPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble()));
        }
        else
        {
            return new Move(new Distance(percepts.getScenarioIntruderPercepts().getMaxMoveDistanceIntruder().getValue() * getSpeedModifier(percepts)));
        }
    }

}
