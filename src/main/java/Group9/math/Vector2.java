package Group9.math;

import Interop.Geometry.Vector;

public class Vector2 {

    private double x, y;

    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public double length()
    {
        return Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2));
    }

    public Vector2 mul(double x, double y)
    {
        return new Vector2(this.x * x, this.y * y);
    }

    public Vector2 add(Vector2 add)
    {
        return this.add(add.getX(), add.getY());
    }

    public Vector2 add(double x, double y)
    {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 sub(Vector2 sub)
    {
        return this.add(-sub.getX(), -sub.getY());
    }

    public Vector2 sub(double x, double y)
    {
        return this.add(-x, -y);
    }

    public double distance(Vector2 other)
    {
        return Math.sqrt(Math.pow(this.x - other.getX(), 2) + Math.pow(this.y - other.getY(), 2));
    }

    public static Vector2 from(Vector point)
    {
        return new Vector2(point.getX(), point.getY());
    }

}