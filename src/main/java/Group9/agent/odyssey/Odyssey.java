package Group9.agent.odyssey;

import Group9.agent.gridbased.CellContent;
import Group9.agent.gridbased.CellPosition;
import Group9.math.Vector2;
import Interop.Percept.Vision.ObjectPerceptType;

public class Odyssey {

    public static void main(String[] args) {
        GridMap gridMap = new GridMap(0.5, 10, 10);
        for(Vector2 cell : gridMap.ray(new Vector2(-3, -3), new Vector2(-3, 0))) {
            gridMap.set(cell.getX(), cell.getY(), new CellContent(gridMap.toCell(cell.getX(), cell.getY()), ObjectPerceptType.Wall));
        }
        for(Vector2 cell : gridMap.ray(new Vector2.Origin(), new Vector2(-3, 0))) {
            gridMap.set(cell.getX(), cell.getY(), new CellContent(gridMap.toCell(cell.getX(), cell.getY()), ObjectPerceptType.Wall));
        }

        System.out.println(gridMap);

    }

}
