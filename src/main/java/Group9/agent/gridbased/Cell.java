package Group9.agent.gridbased;

import java.util.Objects;

public class Cell
{
    private final int x;
    private final int y;

    public Cell(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public int x()
    {
        return this.x;
    }

    public int y()
    {
        return this.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x == cell.x &&
                y == cell.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
