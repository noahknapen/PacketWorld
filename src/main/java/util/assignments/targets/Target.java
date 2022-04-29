package util.assignments.targets;

import environment.Coordinate;

/**
 * A class that represents a target
 */
public abstract class Target {

    private Coordinate coordinate;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Target(Coordinate coordinate) {
        this.setCoordinate(coordinate);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////
    
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return this.coordinate.toString();
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Target target) {
            result = this.coordinate.equals(target.getCoordinate());
        }

        return result;
    }

    @Override
    public int hashCode() {
        return coordinate.hashCode();
    }
}
