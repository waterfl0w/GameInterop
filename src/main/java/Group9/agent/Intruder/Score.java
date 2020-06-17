package Group9.agent.Intruder;

import Group9.map.objects.TargetArea;
import Group9.math.Vector2;
import Group9.tree.PointContainer;

public class Score{

    //TODO: Target Area has to be taken somewhere else
    //targetArea = 140.0,110.0,145.0,110.0,145.0,112.0,140.0,112.0
    TargetArea targetArea = new TargetArea(new PointContainer.Polygon(
            new Vector2(140, 110), new Vector2(145, 110), new Vector2(145, 112), new Vector2(140, 112)));

    // scores for cells with certain features
    private final double TARGET_SCORE = 1000;
    private final double WALL_SCORE = 10;
    private final double TOWER_SCORE = -100;
    private final double DOOR_SCORE = 2;
    private final double WINDOW_SCORE = -500;
    private final double GUARD_SCORE = -500;
    private final double TELEPORT_SCORE = 2;
    //How much the distance influences the score:
    private final double DISTANCE_COEFF = 0;


    // Method to calculate score **might have to add evaluation function adjustments
    public double getScoreCell(Cell cell){
        double scoreOfCell = 0;

        if(cell.hasWall()){scoreOfCell+= WALL_SCORE;}
        if(cell.hasWindow()){scoreOfCell+= WINDOW_SCORE;}
        if(cell.hasDoor()){scoreOfCell+= DOOR_SCORE;}
        if(cell.hasTower()){scoreOfCell+= TOWER_SCORE;}
        if(cell.hasGuard()){scoreOfCell+= GUARD_SCORE;}
        if(cell.hasTarget()){scoreOfCell+= TARGET_SCORE;}
        if(cell.hasTeleport()){scoreOfCell+= TELEPORT_SCORE;}

        //Triangulation
        Vector2 targetCenter = this.targetArea.getContainer().getCenter();
        Vector2 cellCenter = new Vector2(cell.getMidX(), cell.getMidY());
        double distance_x = Math.abs(targetCenter.getX() - cellCenter.getX());
        double distance_y = Math.abs(targetCenter.getY() - cellCenter.getY());
        double distance_to_target = Math.sqrt(distance_x*distance_x + distance_y*distance_y);

        //distance contribute to the score
        scoreOfCell+= DISTANCE_COEFF * distance_to_target;

        //TODO: redefine getCount() in cell
        scoreOfCell+= (-200) * cell.getCount();
        return scoreOfCell;
    }

    // Method to choose best of the available cells regarding the score
    public Cell chooseBestCell(Cell[] cellsToCompare){
        Cell bestCell = cellsToCompare[0];

        for (int i = 1; i < cellsToCompare.length; i++){
            if(getScoreCell(cellsToCompare[i]) > getScoreCell(bestCell)) {
                bestCell = cellsToCompare[i];
            }
        }
        return bestCell;
    }

     /*
    //Attribute
    Cell start;

    //Constructor
    public Intruder1(Cell startPosition)
    {
        this.start = startPosition;
    }
    */


    //////////////////////////////
    //Main
    /*
    Cell target = new Cell(); Object address of memory
    target.setX(3.57688);
    target.setY(3.50009);

    Intruder1 I1 = new Intruder1();

    I1.triangulation(target);   target --> address of Cell Object
    //from here on the object is in target
     */
    ////////////////////////////////

    /*
    //Triangulation method
    public double triangulation(Cell target_coords) { //target_coords --> Cell Object same of target
        //from here on the object is in target_coords
        double target_x = target_coords.getX();
        double target_y = target_coords.getY();

        double my_x = this.start.getX();
        double my_y = this.start.getY();

        double distance_x = Math.abs(target_x - my_x);
        double distance_y = Math.abs(target_y - my_y);
        double distance_to_target = Math.sqrt(distance_x*distance_x + distance_y*distance_y);
        return distance_to_target;
    }
    */
}
