package Group9.agent.gridbased;

import Interop.Percept.Vision.ObjectPerceptType;

public class CellContent {

    private CellPosition cellPosition;
    private ObjectPerceptType type;
    private double logValue = 0;

    public CellContent(CellPosition cellPosition, ObjectPerceptType type)
    {
        this.cellPosition = cellPosition;
        this.type = type;
    }

    public CellPosition getCellPosition() {
        return cellPosition;
    }

    public ObjectPerceptType getType() {
        return type;
    }

    public double getLogValue() {
        return logValue;
    }

    public void updateLog(double value)
    {
        this.logValue += value;
    }

    public boolean isOccupied()
    {
        return true; //TODO
    }

    @Override
    public String toString() {
        if(cellPosition.x() == 0 && cellPosition.y() == 0){
            return "bbbb";
        }
        return "aaaa";
    }
}
