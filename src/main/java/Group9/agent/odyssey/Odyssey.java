package Group9.agent.odyssey;

import Group9.agent.gridbased.CellContent;
import Group9.agent.gridbased.CellPosition;
import Group9.math.Vector2;
import Interop.Percept.Vision.ObjectPerceptType;

public class Odyssey {

    public static void main(String[] args) {
        GridMap gridMap = new GridMap(0.5, 10, 10);

        for(double x = -10; x <= 10; x += 0.5)
        {
            for(double y = -10; y <= 10; y += 0.5)
            {
                gridMap.set(x, y, new CellContent(gridMap.toCell(x, y), ObjectPerceptType.EmptySpace));
            }
        }

        System.out.println(gridMap.path(new Vector2.Origin(), new Vector2.Random().mul(5)));

    }

}
