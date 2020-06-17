package Group9.agent.IntruderAction;
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
import Interop.Percept.Vision.ObjectPercepts;

import javax.swing.*;

public class Agent2 implements Guard {
    private Angle rotation;
    private boolean FoundArea = false;
    private Queue<ActionContainer<GuardAction>> CampingArea = new LinkedList<>();
    private Queue<ActionContainer<GuardAction>> followIntruder = new LinkedList<>();
    private Vector2 position ;
    private boolean foundSentry = false;
    private int NoOfRotations = 10;
    private int count = 0;
    private boolean foundIntruder = false;
    private boolean foundTeleport = false;
    private int count1 = 0;
private boolean set = false;
    public Agent2()
    {
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
//                followIntruder.addAll(moveTowardsIntruder(percepts) );
//            }
//          //  return null;
//            return followIntruder.poll().getAction();
//        }
     /*  if(!foundTeleport )
        {
            for(ObjectPercept object : objectPercepts){
                if (object.getType().equals(ObjectPerceptType.Teleport)){
                    foundTeleport = true;
                    break;
                }
            }
        }

        if(foundTeleport  && percepts.wasLastActionExecuted()){
            if (CampingArea.isEmpty()) {
                  CampingArea.clear();
                CampingArea.addAll(camping(percepts));
            }
                return CampingArea.poll().getAction();
        }*/
      /*if(!foundIntruder)
        {
            for(ObjectPercept object : objectPercepts){
                if (object.getType().equals(ObjectPerceptType.Intruder)){
                    foundIntruder = true;
                    break;
                }
            }
        }
        Vector2 intruderPosition  = predictIntruderMove(percepts);
        if(intruderPosition != null || !followIntruder.isEmpty())
        {
            if(intruderPosition != null)
            {
             //   targetAreaGuarding.clear();
               // followIntruder.clear();
                followIntruder.addAll(
                        moveTowardsPoint(percepts, new Vector2(0, 1), new Vector2.Origin(), intruderPosition)
                );
            }

            if(!followIntruder.isEmpty())
            {
                return followIntruder.poll().getAction();
            }
        }*/

       /* if(foundIntruder  && percepts.wasLastActionExecuted()) {
            System.out.println("DOIN");
                return IntruderAction(percepts);

        }*/
      if(!foundSentry)
        {
            for(ObjectPercept object : objectPercepts){
                if (object.getType().equals(ObjectPerceptType.SentryTower)){
                    foundSentry  = true;
                    break;
                }
            }
        }

        if(foundSentry  && percepts.wasLastActionExecuted()) {
            while(count<NoOfRotations) {
                return sentry(percepts);
            }
        }

        return exploration(percepts);
    }


    private GuardAction exploration(GuardPercepts percepts)
    {

        if(!percepts.wasLastActionExecuted())
        {
            return new Rotate(Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians()* Game._RANDOM.nextDouble()));
        }
        else
        {
            return new Move(new Distance(percepts.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue()*getSpeedModifier(percepts)));
        }
    }


    private Vector2 moveTowardsIntruder(GuardPercepts p )
    {
        Set<ObjectPercept> guardView = p.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.Intruder) )
                .getAll();
        List<Vector2> points = new ArrayList<Vector2>();
        Move move = null;
        Vector2 one = new Vector2.Origin();
        Vector2 two = new Vector2.Origin();
        for(ObjectPercept o : guardView)
        {
            points.add(Vector2.from(o.getPoint()));

        }
        if(points.size()==1)
        {
            return points.get(0);
         //   move = new Move(new Distance(new Point(0,0),points.get(0)));
           // return move;
        }
        else{
            for(int i = 0;i<points.size()-1;i++)
            {
                one = points.get(i);
                two = points.get(i+1);


               // one = points.get(i);
                //two = points.get(i+1);
                //Distance dis = one.getDistance(two);
                // Distance dis2 = new Distance(new Point(0,0),two);
                //double val = dis.getValue();//val of distnace between one and two
                //Point third = new Point(two.getX()+val,two.getY()+val);
                //move = new Move(new Distance(new Point(0,0),two));
                //return move;
            }
            return two.sub(one).mul(2).add(new Vector2.Origin());

         //   Vector2 third = Vector2.from(two).add();
        }
    //    return exploration(p);

    }

  /*  private Vector2 intruder(GuardPercepts p)
    {
        Set<ObjectPercept> guardView = p.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.Intruder) )
                .getAll();
        if(!guardView.isEmpty())
        {
            List<Vector2> points = new ArrayList<>();
            for(ObjectPercept o : guardView) {
                points.add(Vector2.from(o.getPoint()));
                if (points.size() > 1) {
                    for (int i = 0; i < points.size() - 1; i++) {
                        return moveTowardsPoint(p, );
                    }
                }
            }
        }
        return null;
    }
*/

    private GuardAction sentry(GuardPercepts percep)
    {
        Set<ObjectPercept> guardView = percep.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.SentryTower) )
                .getAll();

            if (!guardView.isEmpty()) {
                for (ObjectPercept o : guardView) {
                    Distance d = new Distance(o.getPoint(),new Point(0,0));
                    double value = d.getValue()-1;
                    Move move = new Move(new Distance(o.getPoint(), new Point(0, 0)));
                    if (move.getDistance().getValue() >= percep.getScenarioGuardPercepts().getMaxMoveDistanceGuard().getValue()) {
                        move = new Move(percep.getScenarioGuardPercepts().getMaxMoveDistanceGuard());
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
    }

    private Queue<ActionContainer<GuardAction>> camping(GuardPercepts percepts)
    {
        Set<ObjectPercept> guardView = percepts.getVision()
                .getObjects()
                .filter(e -> e.getType().equals(ObjectPerceptType.Teleport)  )
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
        if(max != null && max.length() >= 0.1)
        {
            return moveTowardsPoint(percepts, new Vector2(0 ,1), new Vector2.Origin(), max);
        }
        Angle newRotation = Angle.fromRadians(percepts.getScenarioGuardPercepts().getScenarioPercepts().getMaxRotationAngle().getRadians() * Game._RANDOM.nextDouble());
        rotation = Angle.fromRadians(rotation.getRadians() + newRotation.getRadians());
        if (rotation.getDegrees() > 360) {
            rotation = Angle.fromDegrees(rotation.getDegrees() - 360);
            count1++;

        } else if (rotation.getDegrees() < 0) {

            rotation = Angle.fromDegrees(rotation.getDegrees() + 360);
            count1++;
        }

        Queue<ActionContainer<GuardAction>> actions = new LinkedList<>();
        actions.add(ActionContainer.of(this, new Rotate(rotation)));

        return actions;

    }

    private Queue<ActionContainer<GuardAction>> predictIntruderMove(GuardPercepts percepts)
    {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();
        if(!intruders.isEmpty()) {
            Vector2 one = new Vector2.Origin();
            Vector2 two = new Vector2.Origin();
            Vector2 guard = new Vector2.Origin();
            List<Vector2> intPos = new ArrayList<>();
            for (ObjectPercept o : intruders) {
                intPos.add(Vector2.from(o.getPoint()));
            }
            if(intPos.size()==1 )
            {
                return  moveTowardsPoint(percepts, new Vector2(0, 1 ), guard, intPos.get(0));
            }
            else
            {
                for(int i = 0;i<intPos.size()-1;i++)
                {
                    one = intPos.get(i);
                    two = intPos.get(i+1);

                    return moveTowardsPoint(percepts,new Vector2(0,1),guard,two.sub(one).mul(1.5).add(guard));
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

    private GuardAction IntruderAction(GuardPercepts percepts)
    {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();
        Queue<ActionContainer<GuardAction>> act = null;

        if(!intruders.isEmpty())
        {
            Vector2 centre = new Vector2.Origin();
            for(ObjectPercept e : intruders)
            {
                centre = centre.add(Vector2.from(e.getPoint()));
                centre = centre.add(Vector2.from(e.getPoint()));
            }
             act = moveTowardsPoint(percepts,new Vector2(0,1),new Vector2.Origin(),centre.mul(1D/intruders.size()));
        }

      //  return null;
      /*  Vector2 pos = new Vector2.Origin();
        List<Vector2> points = new ArrayList<>();
        for(ObjectPercept o: intruders)
        {
            points.add(Vector2.from(o.getPoint()));
            act = moveTowardsPoint(percepts,new Vector2(0,1),new Vector2.Origin(),Vector2.from(o.getPoint()).mul(1/intruders.size()));
        }*/


     //   }
  return act.poll().getAction();
    }
    public Vector2 intruderMove(GuardPercepts percepts)
    {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();
        if(!intruders.isEmpty())
        {
            List<Vector2> points = new ArrayList<Vector2>();

        }

        return null;
    }
    public Vector2 canSeeIntruder(GuardPercepts percepts)
    {
        Set<ObjectPercept> intruders = percepts.getVision().getObjects()
                .filter(e -> e.getType() == ObjectPerceptType.Intruder)
                .getAll();

        if(!intruders.isEmpty())
        {
            List<Vector2> points = new ArrayList<Vector2>();
            Vector2 intPos = new Vector2.Origin();
            for(ObjectPercept e : intruders)
            {
                points.add(Vector2.from(e.getPoint()));
              //  intPos = intPos.add(Vector2.from(e.getPoint()));
            }
            if(points.size()==1)
            {
                return points.get(0);
            }
            else
            {
                return points.get(points.size()-2).sub(points.get(points.size()-1)).mul(1/intruders.size());
            }

         //   return intPos.mul(1D/intruders.size());
        }

        return null;
    }


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
}