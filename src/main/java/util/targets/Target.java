package util.targets;

import environment.Coordinate;

import java.awt.Color;

public class Target {

    protected Coordinate coordinate;
    protected int colorRGB;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public Target(Coordinate coordinate, Color color)
    {
        this.coordinate = coordinate;
        this.colorRGB = color.getRGB();
    }

    /////////////
    // GETTERS //
    /////////////

    public Coordinate getCoordinate()
    {
        return this.coordinate;
    }

    public Color getColor()
    {
        return new Color(this.colorRGB);
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        
        if (object instanceof Target casted)
            result = (this.getCoordinate().equals(casted.getCoordinate()) && this.getColor().equals(casted.getColor()));

        return result;
    } 
}
