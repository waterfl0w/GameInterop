package Group9.agent.gridbased;

import Group9.math.Vector2;

import java.util.Objects;

public class CellPosition
{
    private final int x;
    private final int y;

    public CellPosition(int x, int y)
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
        CellPosition cellPosition = (CellPosition) o;
        return x == cellPosition.x &&
                y == cellPosition.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
