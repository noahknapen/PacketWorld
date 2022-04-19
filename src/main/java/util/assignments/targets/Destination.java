package util.assignments.targets;

import environment.Coordinate;

public class Destination extends Target{
    
    private int rgbColor;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Destination(Coordinate coordinate, int rgbColor) {
        super(coordinate);
        this.setRgbColor(rgbColor);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public int getRgbColor() {
        return rgbColor;
    }

    public void setRgbColor(int rgbColor) {
        this.rgbColor = rgbColor;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }
}
