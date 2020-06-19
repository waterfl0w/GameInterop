package Group9.agent.odyssey;

import Group9.agent.gridbased.CellContent;
import Group9.agent.gridbased.CellPosition;
import Group9.math.Vector2;
import Interop.Percept.Vision.ObjectPerceptType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GridMap {

    //TODO: The Growth Factor cannot be less than 2 right now. This is because the map right now does not have the ability
    // to only grow in one direction.
    private final static double GROWTH_FACTOR = 2;
    private final double resolution;
    private CellContent[][] map;

    private final static double occupiedValue = 20;
    private final static double unoccupiedValue = -0.7;

    public GridMap(double resolution, double initialWidth, double initialHeight){
        this.resolution = resolution;
        this.map = new CellContent[ceil(initialHeight/resolution)][ceil(initialWidth/resolution)];
    }

    public CellContent[][] getCells() {
        return map;
    }

    public double getWidth()
    {
        return this.resolution * this.map[0].length;
    }

    public double getHeight()
    {
        return this.resolution * this.map.length;
    }

    public void update(double x, double y, boolean isOccupied, ObjectPerceptType type)
    {
        CellContent content = get(x, y);

        if(content == null)
        {
            content = new CellContent(toCell(x, y), type);
            set(x, y, content);
        }
        content.updateLog(isOccupied ? occupiedValue : unoccupiedValue);
        if(isOccupied)
        {
            content.setType(type);
        }
    }

    public void set(double x, double y, CellContent value)
    {
        this.checkForGrowth(x, y);
        CellPosition cell = toCell(x, y);
        this.map[cell.y()][cell.x()] = value;
    }

    public CellContent get(double x, double y)
    {
        CellPosition cell = toCell(x, y);
        if(!hasCell(cell))
        {

            return null;
        }
        return cellGet(cell);
    }

    private CellContent cellGet(CellPosition cell)
    {
        return this.map[cell.y()][cell.x()];
    }

    private boolean hasCell(CellPosition cell)
    {
        return (cell.x() < horizontalLength() && cell.y() < verticalLength() && cell.x() >= 0 && cell.y() >= 0);
    }

    public List<Vector2> ray(Vector2 a, Vector2 b)
    {
        //@SOURCE http://algo.pw/algo/69/java
        List<Vector2> cells = new ArrayList<>();
        CellPosition ac = toCell(a.getX(), a.getY());
        CellPosition bc = toCell(b.getX(), b.getY());
        int pdx = 0, pdy = 0, es, el, err;

        int dx = bc.x() - ac.x();
        int dy = bc.y() - ac.y();

        int incx = (int) Math.signum(dx);
        int incy = (int) Math.signum(dy);

        if (dx < 0) dx = -dx;
        if (dy < 0) dy = -dy;

        if (dx > dy)
        {
            pdx = incx;
            es = dy;
            el = dx;
        }
        else
        {
            pdy = incy;
            es = dx;
            el = dy;
        }

        int x = ac.x();
        int y = ac.y();
        err = el/2;
        cells.add(toRealWorld(new CellPosition(x, y)));

        for (int t = 0; t < el; t++)
        {
            err -= es;
            if (err < 0)
            {
                err += el;
                x += incx;
                y += incy;
            }
            else
            {
                x += pdx;
                y += pdy;
            }

            CellPosition position = new CellPosition(x, y);
            if(hasCell(position))
            {
                CellContent cell = cellGet(position);
                if(cell != null && cell.getType().isSolid())
                {
                    break;
                }
            }

            cells.add(toRealWorld(position));
        }
        return cells;
    }

    public List<Vector2> path(Vector2 start, Vector2 target)
    {
        CellPosition startCell = toCell(start.getX(), start.getY());
        CellPosition targetCell = toCell(target.getX(), target.getY());

        Map<CellPosition, Double> fScore = new HashMap<>();
        fScore.put(startCell, h(startCell, targetCell));

        List<CellPosition> openSet = new LinkedList<>();
        openSet.add(startCell);

        Map<CellPosition, CellPosition> cameFrom = new HashMap<>();

        Map<CellPosition, Double> gScore = new HashMap<>();
        gScore.put(startCell, 0D);

        while (!openSet.isEmpty())
        {
            CellPosition current = openSet.get(0);

            if(current.equals(targetCell))
            {
                System.out.println("done");
                List<Vector2> total_path = new LinkedList<>();
                total_path.add(toRealWorld(current));
                while (cameFrom.containsKey(current))
                {
                    current = cameFrom.get(current);
                    total_path.add(toRealWorld(current));
                }
                Collections.reverse(total_path);
                return total_path;
            }

            openSet.remove(0);

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    CellPosition neighbour = new CellPosition(current.x() + x, current.y() + y);
                    CellContent cell = cellGet(neighbour);
                    if(!this.hasCell(neighbour) || cell == null || cell.isOccupied()) continue;

                    double tentative_gScore = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + 0;
                    if(tentative_gScore < gScore.getOrDefault(neighbour, Double.POSITIVE_INFINITY))
                    {
                        cameFrom.put(neighbour, current);
                        gScore.put(neighbour, tentative_gScore);
                        fScore.put(neighbour, tentative_gScore + h(neighbour, targetCell));
                        if(!openSet.contains(neighbour))
                        {
                            openSet.add(neighbour);
                        }
                    }
                }
            }

            openSet.sort(Comparator.comparingDouble(o -> fScore.getOrDefault(o, Double.POSITIVE_INFINITY)));

        }
        return null;
    }

    private double h(CellPosition cell, CellPosition target)
    {
        //return 0; // Equivalent to Dijkstra
        return Math.sqrt(Math.pow(cell.x() - target.x(), 2) + Math.pow(cell.y() - target.y(), 2)); //Euclidian distance
        //return Math.abs(cell.x() - cell.y()) + Math.abs(target.x() - target.y()); //Manhattan distance
    }

    public CellPosition toCell(double x, double y)
    {
        return new CellPosition(
                floor((x + getWidth() / 2) / resolution),
                floor((y + getHeight() / 2) / resolution)
        );
    }

    public Vector2 toRealWorld(CellPosition cellPosition)
    {
        //TODO move to the proper centre of the cell
        return new Vector2(
                ((cellPosition.x() * resolution) - getWidth() / 2) + resolution / 2,
                ((cellPosition.y() * resolution) - getHeight() / 2) + resolution / 2
        );
    }

    private void checkForGrowth(double x, double y)
    {
        //--- calculate the cell position. use the abs values to avoid handling negative values further down in the pipeline
        CellPosition cell = toCell(Math.abs(x), Math.abs(y));
        if(cell.x() >= horizontalLength() && cell.y() >= verticalLength())
        {
            grow(ceil(cell.x() * GROWTH_FACTOR), ceil(cell.y() * GROWTH_FACTOR));
        }
        else if(cell.x() >= horizontalLength())
        {
            grow(ceil(cell.x() * GROWTH_FACTOR), verticalLength());
        }
        else if(cell.y() >= verticalLength())
        {
            grow(horizontalLength(), ceil(cell.y() * GROWTH_FACTOR));
        }
    }

    public int verticalLength()
    {
        return this.map.length;
    }

    public int horizontalLength()
    {
        return this.map[0].length;
    }

    private void grow(int newWidth, int newHeight)
    {
        long time = System.currentTimeMillis();
        CellPosition oldCenter = toCell(0, 0);
        CellContent[][] newMap = new CellContent[newHeight][newWidth];
        CellContent[][] oldMap = this.map;
        this.map = newMap;
        CellPosition newCenter = toCell(0, 0);

        final int xOffset = newCenter.x() - oldCenter.x();
        final int yOffset = newCenter.y() - oldCenter.y();

        for (int y = 0; y < oldMap.length; y++)
        {
            System.arraycopy(oldMap[y], 0, newMap[y + yOffset], xOffset, oldMap[y].length);
            oldMap[y] = null;
        }

        System.out.println("grow time: " + (System.currentTimeMillis() - time) + " grow to " + horizontalLength() + "x" + verticalLength());

    }

    public void writeDebugImage()
    {
        BufferedImage bufferedImage = new BufferedImage(horizontalLength(), verticalLength(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < horizontalLength(); x++)
        {
            for(int y = 0; y < verticalLength(); y++)
            {
                int pixel = 0;
                CellContent cellContent = this.map[y][x];
                if(cellContent != null)
                {
                    if(cellContent.getType().isSolid())
                    {
                        pixel = Color.BLUE.getRGB();
                    }
                    else
                    {
                        pixel = Color.WHITE.getRGB();
                    }
                }
                bufferedImage.setRGB(x, y, pixel);
            }
        }
        File outputfile = new File("saved.png");
        try {
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        CellPosition centre = toCell(0, 0);
        for(int y = 0; y < verticalLength(); y++)
        {
            for (int x = 0; x < horizontalLength(); x++) {
                builder.append(((y == centre.y() && x == centre.x()) ? "cccc" : this.map[y][x]) + " ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    private static int floor(double a)
    {
        return (int) Math.floor(a);
    }

    private static int ceil(double a)
    {
        return (int) Math.ceil(a);
    }

}
