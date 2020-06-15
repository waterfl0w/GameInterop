package Group9.agent.Intruder;

import Interop.Percept.Vision.ObjectPercept;
import Interop.Percept.Vision.ObjectPerceptType;

import java.util.HashMap;
import java.util.LinkedList;

public class Cell {
    //x,y are the corners of the left top corner of a cell
    private double x;
    private double y;
    // midX,midY represent the middle of the square
    private double midX;
    private double midY;
    //The cells above, below, left, and right represent the cells that are in the indicated directions. They are null if unexplored
    private Cell above;
    private Cell below;
    private Cell left;
    private Cell right;
    private final static double size = 1;
    private double exploredPercentage;
    private boolean reachable;
    public static int numOfCells = 0;
    private int visitedCount;
    private boolean processed;
    private boolean printed;

    private boolean wall = false;
    private boolean window = false;
    private boolean door = false;
    private boolean guard = false;
    private boolean sentryTower = false;
    private boolean target = false;
    private boolean teleport = false;
    public Cell(double x, double y)
    {
        above = null;
        below = null;
        left = null;
        right = null;
        setMidX(x);
        setMidY(y);
        numOfCells++;
        processed = false;
    }
    public Cell()
    {
        above = null;
        below = null;
        left = null;
        right = null;
        processed = false;
    }
    public LinkedList<Cell> getUnprocessed(History history)
    {
        LinkedList<Cell> unprocessed = new LinkedList<>();
        Cell above = history.getCell(new Coordinate(getMidX(), getMidY() + 1));
        Cell below =history.getCell(new Coordinate(getMidX(), getMidY() - 1));
        Cell left = history.getCell(new Coordinate(getMidX()-1, getMidY()));
        Cell right = history.getCell(new Coordinate(getMidX()+1, getMidY()));
        if (above == null)
        {
            above = new Cell(getMidX(), getMidY() + 1);
            System.out.println("added: " + above);
        }
        if (below == null)
        {
            below = new Cell(getMidX(), getMidY() -1);
            System.out.println("added: " + below);
        }
        if(left == null)
        {
            left = new Cell(getMidX() -1, getMidY());
            System.out.println("added: " + left);
        }
        if(right == null)
        {
            right = new Cell(getMidX() +1, getMidY());
            System.out.println("added: " + right);
        }
        if (!above.getProcessed())
        {
            unprocessed.add(above);
        }
        if (!below.getProcessed())
        {
            unprocessed.add(below);
        }
        if (!left.getProcessed())
        {
            unprocessed.add(below);
        }
        if (!right.getProcessed())
        {
            unprocessed.add(right);
        }
         return unprocessed;
    }
    public double getScore()
    {
        //calculate the score;
        Score score = new Score();

        return score.getScoreCell(this);
    }

    /**
     * Finds the cell closest to the given coordinates
     * @param x the x coordinate of the cell
     * @param y the y coordinate of the cell
     * @return the cell closest to the given coordinates
     */
    public Cell find(double x, double y)
    {
        x = Math.round(x);
        y = Math.round(y);
        Cell result = this;
        while(result.getMidX() != x && result.getMidY() != y)
        {
            //looking for the cell with the right x coordinate
            if(x > result.getMidX())
            {
                if (result.getRight() != null)
                {
                    result = result.getRight();
                }
                else
                {
                    x = result.getX();
                }
            }
            else if(x < result.getMidX())
            {
                if (result.getLeft() != null)
                {
                    result = result.getLeft();
                }
                else
                {
                    x = result.getMidX();
                }
            }

            // looking for the cell with the right y coordinate
            if(y > result.getMidY())
            {
                if (result.getBelow() != null)
                {
                    result = result.getBelow();
                }
                else
                {
                    y = result.getY();
                }
            }
            else if(y < result.getMidY())
            {
                if (result.getAbove() != null)
                {
                    result = result.getAbove();
                }
                else
                {
                    y = result.getMidY();
                }
            }
        }
        return  result;
    }
    public Cell findNullable(double x, double y)
    {
        Cell result = find(x,y);
        if (result.getMidX() != x || result.getMidY() != y)
        {
            return null;
        }
        else
        {
            return  result;
        }
    }
    public void deProcess()
    {
        processed = false;
    }
    public boolean getProcessed()
    {
        return processed;
    }
    public void setProcessed(boolean b)
    {
        this.processed = b;
    }
    public void setUnprocessed()
    {
        processed = false;
        if (above != null && above.getProcessed())
        {
            above.setUnprocessed();
        }
        if (below != null && below.getProcessed())
        {
            below.setUnprocessed();
        }
        if (left != null && left.getProcessed())
        {
            left.setUnprocessed();
        }
        if (right != null && right.getProcessed())
        {
            right.setUnprocessed();
        }
    }
    public void setAllNotPrinted()
    {
        printed = false;
        if (above != null && above.getPrinted())
        {
            above.setAllNotPrinted();
        }
        if (below != null && below.getPrinted())
        {
            below.setAllNotPrinted();
        }
        if (left != null && left.getPrinted())
        {
            left.setAllNotPrinted();
        }
        if (right != null && right.getPrinted())
        {
            right.setAllNotPrinted();
        }
    }
    public void printall(int x, int y)
    {
        printed = true;
        if (above != null && !above.getPrinted())
        {
            above.printall(x, y+1);
        }
        if (below != null && !below.getPrinted())
        {
            below.printall(x, y-1);
        }
        if (left != null && !left.getPrinted())
        {
            left.printall(x -1, y);
        }
        if (right != null && !right.getPrinted())
        {
            right.printall(x + 1, y);
        }
    }
    public boolean getPrinted()
    {
        return printed;
    }
    public void setAbove(Cell above) {
        this.above = above;
    }

    public void setBelow(Cell below) {
        this.below = below;
    }

    public void setLeft(Cell left) {
        this.left = left;
    }

    public void setRight(Cell right) {
        this.right = right;
    }
    public Cell addAbove()
    {
        Cell above = new Cell(getMidX(), getMidY() +1);
        numOfCells++;
        return above;
    }
    public int getNumOfCells()
    {
        return numOfCells;
    }
    public Cell addBelow()
    {
        Cell below = new Cell(getMidX(), getMidY() -1);
        numOfCells++;
        return below;
    }
    public Cell addLeft()
    {
        if (this.getLeft() == null)
        {
            Cell left = new Cell(this.getMidX() - size, this.getMidY());
            setLeft(left);
            left.setRight(this);
            left.setLeft(findNullable(getMidX() - 2 * size, getMidY()));
            left.setAbove(findNullable(getMidX() - size, getMidY() + size));
            left.setBelow(findNullable(getMidX() - size, getMidY() - size));
            numOfCells++;
        }
        return this.getLeft();
    }
    public Cell addRight()
    {
        if(this.getRight() == null)
        {
            Cell right = new Cell(this.getMidX() + size, this.getMidY());
            setRight(right);
            right.setLeft(this);
            right.setRight(findNullable(getMidX() + 2 * size, getMidY()));
            right.setAbove(findNullable(getMidX() + size, getMidY() + size));
            right.setBelow(findNullable(getMidX() + size, getMidY() - size));
            numOfCells++;
        }
        return this.getRight();
    }

    public void setMidX(double midX) {
        this.midX = midX;
        this.x = midX - .5 * size;
    }

    public void setMidY(double midY) {
        this.midY = midY;
        this.y = midY - .5 * size;
    }

    public void setX(double x) {
        this.x = x;
        this.midX = x + .5 * size;
    }

    public void setY(double y) {
        this.y = y;
        this.midY = y + .5 * size;
    }

    public Cell getAbove()
    {
        return above;
    }

    public Cell getBelow() {
        return below;
    }

    public Cell getLeft() {
        return left;
    }

    public Cell getRight() {
        return right;
    }

    public double getMidX() {
        return midX;
    }

    public double getMidY() {
        return midY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public Coordinate[] getPoints()
    {
        return new Coordinate[]{new Coordinate(midX + 0.5 * size, midY + 0.5 * size), new Coordinate(midX - 0.5 * size, midY + 0.5 * size), new Coordinate(midX - 0.5 * size, midY - 0.5 * size), new Coordinate(midX + 0.5 * size, midY - 0.5 * size)};
    }
    public int getCount(){
        return visitedCount;
    }
    public void setObject(ObjectPerceptType type)
    {
        switch (type)
        {
            case Door:
            {
                door = true;
                break;
            }
            case Wall:
            {
                wall = true;
                break;
            }
            case Guard:
            {
                guard = true;
                break;
            }
            case Window:
            {
                window = true;
                break;
            }
            case Teleport:
            {
                teleport = true;
                break;
            }
            case TargetArea:
            {
                target = true;
                break;
            }
            case SentryTower:
            {
                sentryTower = true;
                break;
            }
            default:
            {
                //do nothing
                break;
            }
        }
    }
    public boolean hasWall(){
        return wall;
    }
    public boolean hasDoor(){
        return door;
    }

    public boolean hasWindow(){
        return window;
    }
    public boolean hasTower(){
        return sentryTower;
    }
    public boolean hasGuard(){
        return guard;
    }
    public boolean hasTarget(){
        return target;
    }

    public boolean hasTeleport(){
        return teleport;
    }
    public void addVisitedCount()
    {
        visitedCount++;
    }
    @Override
    public String toString()
    {
        return "Cell(x,y) " + getMidX() + ", " + getMidY() + " id: "+ super.toString();
    }
}
